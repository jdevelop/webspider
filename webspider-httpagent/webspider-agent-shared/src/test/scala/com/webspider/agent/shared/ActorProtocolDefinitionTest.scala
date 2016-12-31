package com.webspider.agent.shared

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.{Actor, ActorPath, ActorSystem, Cancellable, Kill, OneForOneStrategy, Props, SupervisorStrategy}
import akka.routing.{Broadcast, RoundRobinPool}
import akka.testkit.TestKit
import akka.util.Timeout
import com.webspider.agent.shared.ActorProtocolDefinition.{Messages, TaskTypesDefinition}
import org.junit.runner.RunWith
import org.scalacheck.Gen
import org.scalacheck.rng.Seed
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSpec, ShouldMatchers}

import scala.collection.mutable
import scala.concurrent.Await

/**
  * User: Eugene Dzhurinsky
  * Date: 12/16/16
  */
object ActorProtocolDefinitionTest {

  val system = ActorSystem("TestProtocol")

  trait TestTaskTypes extends TaskTypesDefinition {
    override type TaskT = String
    override type ResultT = (String, String)
    override type ErrorT = String
  }

  import concurrent.duration._

  class TestConsmerActor extends Actor with ActorProtocolDefinition.ConsumerActorTrait with TestTaskTypes {

    override def receive: Receive = consumePrototype(timeout = Timeout(1 second))

    override protected def payloadAction(task: String): Either[String, (String, String)] = {
      Right((task, self.path.toString))
    }
  }

  class TestProducerActor(queue: mutable.Queue[String],
                          results: mutable.Queue[(String, String)],
                          errors: mutable.Queue[String],
                          expectedInvocations: CountDownLatch
                         ) extends Actor
    with ActorProtocolDefinition.ProducerActorTrait with TestTaskTypes {

    override def receive: Receive = producePrototype

    override protected def hasMoreTasks: Boolean = {
      queue.nonEmpty
    }

    override protected def nextTask: Option[String] = Option(queue.dequeue())

    override protected def enqueueTask(task: String): Unit = queue.enqueue(task)

    override protected def processResponse(src: Either[String, (String, String)]): Unit = {
      src match {
        case Left(msg) ⇒ errors.enqueue(msg)
        case Right(tuple) ⇒ results.enqueue(tuple)
      }
      expectedInvocations.countDown()
    }
  }


}

@RunWith(classOf[JUnitRunner])
class ActorProtocolDefinitionTest extends FunSpec
  with BeforeAndAfterAll
  with ShouldMatchers {

  import ActorProtocolDefinitionTest._


  describe("ActorProtocolDefinition") {

    val seed = Seed.random()

    val strings = Iterator.iterate(seed)(_.next)
      .map(s ⇒ Gen.alphaNumStr.apply(Gen.Parameters.default.withSize(20), s))
      .filter(_.isDefined)
      .take(100).flatten.toSeq


    /**
      * a producer is initialized with a queue of N items
      * M of consumers are started under some round-robin router
      * the producer sends a broadcast message to the path
      * every consumer tries to get a message and return some response
      * as a result - all the messages should be processed from the queue, no matter how many actors failed to process them
      */
    it("should test 1 producer - multiple consumers, no failures") {

      val actorNumber = 20

      val queue = strings.foldLeft(mutable.Queue[String]()) {
        case (q, m) ⇒
          q.enqueue(m)
          q
      }

      val results = mutable.Queue[(String, String)]()
      val errors = mutable.Queue[String]()

      import scala.concurrent.duration._

      val expectedInvocations = new CountDownLatch(strings.size)

      val producer = system.actorOf(Props(
        new TestProducerActor(queue, results, errors, expectedInvocations) {

          var internalScheduler: Cancellable = _

          def initHandler: Receive = {
            case path: ActorPath ⇒
              val actorFor = context.system.actorSelection(path)

              import context.dispatcher

              internalScheduler = context.system.scheduler.schedule(500 millis, 1 second, new Runnable {
                override def run() = {
                  actorFor.!(Broadcast(ActorProtocolDefinition.Messages.ProtocolBeacon))(self)
                }
              })
          }

          override def receive: Receive = initHandler orElse super.receive

          override protected def hasMoreTasks: Boolean = {
            val resp = super.hasMoreTasks
            if (!resp) {
              internalScheduler.cancel()
            }
            resp
          }
        })
      )

      val consumer = system.actorOf(Props(classOf[TestConsmerActor]).withRouter(RoundRobinPool(actorNumber)))

      producer ! consumer.path

      expectedInvocations.await(30, TimeUnit.SECONDS) should be(true)

      strings should contain theSameElementsAs results.map(_._1)
      errors should be(empty)

      val distinctActorPaths = results.map(_._2).distinct
      distinctActorPaths.length should be(actorNumber)

    }

    it("should perform continuous flow of request-response messages even if no beacon schedule is available") {

      val queue = strings.foldLeft(mutable.Queue[String]()) {
        case (q, m) ⇒
          q.enqueue(m)
          q
      }

      val results = mutable.Queue[(String, String)]()
      val errors = mutable.Queue[String]()

      val expectedInvocations = new CountDownLatch(strings.size)

      val producer = system.actorOf(Props(
        new TestProducerActor(queue, results, errors, expectedInvocations) {

          def initHandler: Receive = {
            case path: ActorPath ⇒
              val actorFor = context.system.actorSelection(path)
              actorFor.!(ActorProtocolDefinition.Messages.ProtocolBeacon)(self)
          }

          override def receive: Receive = initHandler orElse super.receive

        }), "Producer"
      )

      val consumer = system.actorOf(Props(classOf[TestConsmerActor]), "Consumer")

      producer ! consumer.path

      val allDone = expectedInvocations.await(5, TimeUnit.SECONDS)

      println(allDone)

      if (!allDone) {
        expectedInvocations.getCount should be(0L)
      }


      strings.length should be(results.length)

      strings should contain theSameElementsAs results.map(_._1)
      errors should be(empty)

      val distinctActorPaths = results.map(_._2).distinct
      distinctActorPaths.length should be(1)
    }

    /**
      * a producer is initialized with a queue of N items
      * M of consumers are started under some round-robin router
      * the producer sends a broadcast message to the path
      * every consumer tries to get a message, and fails with the probability of 1/3 (kills itself)
      * as a result - all the messages should be processed from the queue, no matter how many actors failed to process them
      */
    it("should test 1 producer - multiple consumers, some failures") {
      val queue = strings.foldLeft(mutable.Queue[String]()) {
        case (q, m) ⇒
          q.enqueue(m)
          q
      }

      val results = mutable.Queue[(String, String)]()
      val errors = mutable.Queue[String]()

      val expectedInvocations = new CountDownLatch(strings.size)

      val producer = system.actorOf(Props(
        new TestProducerActor(queue, results, errors, expectedInvocations) {

          def initHandler: Receive = {
            case path: ActorPath ⇒
              val actorFor = context.system.actorSelection(path)
              actorFor.!(Broadcast(ActorProtocolDefinition.Messages.ProtocolBeacon))(self)
          }

          override def receive: Receive = initHandler orElse super.receive

        }), "Producer1"
      )

      import scala.concurrent.duration._

      val groupStrategy = OneForOneStrategy() {
        case x: Exception ⇒
          SupervisorStrategy.Stop
      }

      val exceptions = new AtomicInteger(0)

      val actorId = new AtomicInteger(0)

      val consumer = system.actorOf(Props(
        new Actor with ActorProtocolDefinition.ConsumerActorTrait with TestTaskTypes {

          val MyNumber = actorId.incrementAndGet()

          @volatile var actionError = false

          override def receive = {
            case Messages.ProtocolBeacon if MyNumber % 2 == 0 ⇒

              import akka.pattern.ask

              val f = sender.?(Messages.PullTaskRequest(self))(timeout = Timeout(1 second))
              Await.result(f, 2 second)
              self ! Kill
            case x: Any ⇒ consumePrototype(timeout = Timeout(1 second))(x)
          }

          override protected def payloadAction(task: String): Either[String, (String, String)] = {
            if (actionError) {
              Console.err.println(s"ERROR ${
                self
              }")
              actionError = false
              exceptions.incrementAndGet()
              throw new Exception("OMG")
            } else {
              Console.err.println(s"RECOVER ${
                self
              }")
              actionError = true
              Right((task, self.path.toString))
            }
          }
        }

      ).withRouter(RoundRobinPool(5).withSupervisorStrategy(groupStrategy)), "Consumer1")

      producer ! consumer.path

      val allDone = expectedInvocations.await(5, TimeUnit.SECONDS)

      if (!allDone) {
        expectedInvocations.getCount should be(0L)
      }

      strings.length should be(results.length)

      strings should contain theSameElementsAs results.map(_._1)
      errors should be(empty)

      val distinctActorPaths = results.map(_._2).distinct
      distinctActorPaths.length should be(3)

      exceptions.get() should be > 5

    }

  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}