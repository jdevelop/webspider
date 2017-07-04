package com.webspider.agent.node

import java.io.InputStream

import akka.actor.{Actor, ActorSystem, AddressFromURIString, Props}
import akka.cluster.Cluster
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.webspider.agent.shared.WebcrawlerProtocol._
import com.webspider.agent.shared.{ActorProtocolDefinition, CLIHelper, ClusterProtocol, ClusterWatcher}
import com.webspider.config.Config
import com.webspider.parser.link.{ApacheCommonsLinkNormalizer, RelativeLinkNormalizer}
import com.webspider.parser.{DocumentParser, Href, HtmlParser, TypedResource}
import com.webspider.transport.TransportTrait
import com.webspider.transport.http.{HTTPClient, HttpTransport}
import org.rogach.scallop.ScallopConf


/**
  * User: Eugene Dzhurinsky
  * Date: 12/24/16
  */
object HttpClientNode {

  type DocumentParserProvider = (String) ⇒ DocumentParser[InputStream]

  private class Worker(transport: TransportTrait,
                       linkExtractorProvider: DocumentParserProvider) extends Actor
    with ActorProtocolDefinition.ConsumerActorTrait
    with ClusterProtocol.ClusterSubscriber {

    override protected val TopicName: String = "Topic.Url.Requests"

    override type TaskT = ResourceRequest[TypedResource]
    override type ResultT = ResourceResponse[TypedResource]
    override type ErrorT = Exception

    import scala.concurrent.duration._

    @scala.throws[Exception](classOf[Exception])
    override def preStart(): Unit = {
      registerTopicWatch()
      super.preStart()
    }

    override def onSubscribeDone(): Unit = {
      Console.out.println("Cluster registration complete")
    }

    override def receive: Receive = consumePrototype(Timeout(5 seconds))

    override protected def payloadAction(task: ResourceRequest[TypedResource]): Either[Exception, ResourceResponse[TypedResource]] = {
      try {
        transport.retrieveDocument[Either[Exception, ResourceResponse[TypedResource]]](task.url.src) {
          case Left(err) ⇒
            val code = transport.extractErrorCode(err)
            val message = transport.extractErrorMessage(err)
            Right(ResourceResponse(Href(task.url.src), task.url.src, ResponseStatusCode(code, message)))
          case Right(doc) ⇒
            val linkExtractor = linkExtractorProvider(task.url.src)
            val results = linkExtractor.parse(
              doc.is
            )
            Right(ResourceResponse(task.url, task.url.src, ResponseStatusOk, results))
        }
      } catch {
        case e: Exception ⇒ Left(e)
      }
    }
  }

  def main(args: Array[String]): Unit = {

    object CLI extends ScallopConf(args) with CLIHelper.ClusterParams with CLIHelper.ErrorReporter

    CLI.verify()

    val clusterConfig = CLI.clusterConf()

    val akkaConf = ConfigFactory.parseString(
      s"""akka.remote.netty.tcp.port=${clusterConfig.port}
         |akka.remote.netty.tcp.hostname=${clusterConfig.host}
         |akka.remote.netty.tcp.bind-hostname=${clusterConfig.bindHost}
         |akka.remote.netty.tcp.bind-port=${clusterConfig.bindPort}
         |
      """.stripMargin).withFallback(Config.cfg)

    val actorSystem = ActorSystem("WebspiderCluster", akkaConf)

    val c = Cluster(actorSystem)

    val transportTrait = HttpTransport.Get(HTTPClient.client)

    val linkExtractorProvider: DocumentParserProvider = {
      url ⇒
        new HtmlParser(url) {
          override val linkNormalizer: RelativeLinkNormalizer = ApacheCommonsLinkNormalizer
        }
    }

    if (CLI.seedNodes.isSupplied) {
      c.joinSeedNodes(CLI.seedNodes().map(AddressFromURIString.apply))
    }

    c.registerOnMemberUp {
      Console.out.println("Starting the cluster")

      actorSystem.actorOf(Props(new Worker(transportTrait, linkExtractorProvider)).withRouter(RoundRobinPool(5)), "webspider_node")

      ClusterWatcher.registerRestartJVMWatcherActor(actorSystem)

      ClusterWatcher.onRemovedEvent(c)

    }

  }

}