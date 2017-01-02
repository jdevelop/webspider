package com.webspider.agent.node

import java.io.InputStream

import akka.actor.Actor
import akka.util.Timeout
import com.webspider.agent.shared.WebcrawlerProtocol._
import com.webspider.agent.shared.{ActorProtocolDefinition, ClusterProtocol}
import com.webspider.parser.{DocumentParser, TypedResource}
import com.webspider.transport.TransportTrait


/**
  * User: Eugene Dzhurinsky
  * Date: 12/24/16
  */
object HttpClientNode {

  private class Worker(transport: TransportTrait,
                       linkExtractor: DocumentParser[InputStream]) extends Actor
    with ActorProtocolDefinition.ConsumerActorTrait
    with ClusterProtocol.ClusterSubscriber {

    override protected val TopicName: String = "Topic.Url.Requests"

    override type TaskT = ResourceRequest
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

    override protected def payloadAction(task: ResourceRequest): Either[Exception, ResourceResponse[TypedResource]] = {
      try {
        transport.retrieveDocument[Either[Exception, ResourceResponse[TypedResource]]](task.url) {
          case Left(err) ⇒
            val code = transport.extractErrorCode(err)
            val message = transport.extractErrorMessage(err)
            Right(ResourceResponse(task.url, task.url, ResponseStatusCode(code, message)))
          case Right(doc) ⇒
            val results = linkExtractor.parse(
              doc.is
            )
            Right(ResourceResponse(task.url, task.url, ResponseStatusOk, results))
        }
      } catch {
        case e: Exception ⇒ Left(e)
      }
    }
  }

}
