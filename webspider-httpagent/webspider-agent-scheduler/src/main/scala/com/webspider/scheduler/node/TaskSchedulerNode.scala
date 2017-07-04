package com.webspider.scheduler.node

import java.util.UUID

import akka.actor.{Actor, ActorSystem, AddressFromURIString, Props}
import akka.cluster.Cluster
import akka.routing.RoundRobinPool
import com.webspider.agent.shared.ActorProtocolDefinition.Messages.ProtocolBeacon
import com.webspider.agent.shared.WebcrawlerProtocol.ResourceResponse
import com.webspider.agent.shared._
import com.webspider.core.{Href, TypedResource}
import org.rogach.scallop.ScallopConf

import scala.collection.mutable

object TaskSchedulerNode extends Bootstrap.BootstrapNode {

  case class SeedUrl(url: String)

  def main(args: Array[String]): Unit = {

    object Conf extends ScallopConf(args) with CLIHelper.ClusterParams with CLIHelper.ErrorReporter

    Conf.verify()

    val (c, actorSystem) = configure(Conf.clusterConf(), Conf.seedNodes())

    if (Conf.seedNodes.isSupplied) {
      c.joinSeedNodes(Conf.seedNodes().map(AddressFromURIString.apply))
    }

    start(c, actorSystem)

  }

  def start(c: Cluster, actorSystem: ActorSystem): Unit = {
    c.registerOnMemberUp {
      Console.out.println("Scheduler : Starting the cluster")

      actorSystem.actorOf(Props(new Worker()), "scheduler_node")

      ClusterWatcher.registerRestartJVMWatcherActor(actorSystem)

      ClusterWatcher.onRemovedEvent(c)

    }

  }

  private class Worker() extends Actor
    with ActorProtocolDefinition.ProducerActorTrait
    with ClusterProtocol.ClusterPublisher {

    private var currentTask: Option[String] = None

    private val queue = new mutable.HashSet[TaskT]()

    private val processed = new mutable.HashSet[TaskT]()

    override def receive: Receive = producePrototype orElse {
      case SeedUrl(url) if currentTask.isEmpty ⇒
        currentTask = Some(url)
        queue.add(Href(url))
        publish(ProtocolBeacon, "Topic.Url.Requests")
        sender ! Some(UUID.randomUUID())
      case _ ⇒
        sender ! None
    }

    override protected def hasMoreTasks: Boolean = queue.isEmpty

    override protected def nextTask: Option[TaskT] = {
      val head = processed.headOption
      head.foreach(queue.remove)
      head
    }

    override protected def enqueueTask(task: TaskT): Unit = {
      queue.add(task)
    }

    override protected def processResponse(src: Either[ErrorT, ResultT]): Unit = {
      src match {
        case Right(ResourceResponse(url, _, _, inners)) ⇒
          processed.add(url)
          inners
            .filterNot(res ⇒ processed.contains(res))
            .foreach(enqueueTask)
        case Left(err) ⇒
      }
    }

    override type TaskT = TypedResource
    override type ResultT = ResourceResponse[TypedResource]
    override type ErrorT = Exception

  }

}