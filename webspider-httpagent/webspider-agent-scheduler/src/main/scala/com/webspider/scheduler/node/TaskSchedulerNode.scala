package com.webspider.scheduler.node

import java.util.UUID

import akka.actor.{Actor, ActorSystem, AddressFromURIString, Props}
import akka.cluster.Cluster
import akka.routing.RoundRobinPool
import com.typesafe.config.ConfigFactory
import com.webspider.agent.shared.ActorProtocolDefinition.Messages.ProtocolBeacon
import com.webspider.agent.shared.WebcrawlerProtocol.{ResourceRequest, ResourceResponse}
import com.webspider.agent.shared.{ActorProtocolDefinition, CLIHelper, ClusterProtocol, ClusterWatcher}
import com.webspider.config.Config
import com.webspider.parser.{Href, TypedResource}
import org.rogach.scallop.ScallopConf

import scala.collection.mutable

object TaskSchedulerNode {

  case class SeedUrl(url: String)

  def main(args: Array[String]): Unit = {

    object Conf extends ScallopConf(args) with CLIHelper.ClusterParams with CLIHelper.ErrorReporter

    Conf.verify()

    val clusterConfig = Conf.clusterConf()

    val akkaConf = ConfigFactory.parseString(
      s"""akka.remote.netty.tcp.port=${clusterConfig.port}
         |akka.remote.netty.tcp.hostname=${clusterConfig.host}
         |akka.remote.netty.tcp.bind-hostname=${clusterConfig.bindHost}
         |akka.remote.netty.tcp.bind-port=${clusterConfig.bindPort}
         |
      """.stripMargin).withFallback(Config.cfg)

    val actorSystem = ActorSystem("WebspiderCluster", akkaConf)

    val c = Cluster(actorSystem)

    if (Conf.seedNodes.isSupplied) {
      c.joinSeedNodes(Conf.seedNodes().map(AddressFromURIString.apply))
    }

    c.registerOnMemberUp {
      Console.out.println("Starting the cluster")

      actorSystem.actorOf(Props(new Worker()).withRouter(RoundRobinPool(10)), "scheduler_node")

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