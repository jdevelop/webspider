package com.webspider.agent.shared

import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.{Actor, ActorSystem, Props}
import akka.cluster.Cluster
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, ShouldMatchers}
import org.scalatest.junit.JUnitRunner

/**
  * User: Eugene Dzhurinsky
  * Date: 12/27/16
  */
object ClusterProtocolTest {

  val sys = ActorSystem("TestClusterProtocol", ConfigFactory.load("test-cluster"))

}


@RunWith(classOf[JUnitRunner])
class ClusterProtocolTest extends TestKit(ClusterProtocolTest.sys)
  with FunSpecLike
  with ImplicitSender
  with BeforeAndAfterAll
  with ShouldMatchers {

  describe("ClusterProtocol") {

    it("should subscribe and receive messages from and to cluster") {

      Cluster(system)

      val waitForUp = new CountDownLatch(1)

      system.actorOf(Props(new Actor with ClusterProtocol.ClusterSubscriber {
        override protected val TopicName: String = "ololo"


        override def onSubscribeDone(): Unit = {
          waitForUp.countDown()
        }

        override def preStart(): Unit = {
          register()
        }

        override def receive: Receive = {
          case txt: String ⇒
            sender ! txt.reverse
        }
      }))

      waitForUp.await(1, TimeUnit.SECONDS) should be(true)

      system.actorOf(Props(new Actor with ClusterProtocol.ClusterPublisher {


        @scala.throws[Exception](classOf[Exception])
        override def preStart(): Unit = {
          publish("Oh lol", "ololo")
        }

        override def receive: Receive = {
          case response: String ⇒
            testActor ! response
        }

      }))

      import scala.concurrent.duration._
      expectMsg(2 seconds, "lol hO")

    }
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}