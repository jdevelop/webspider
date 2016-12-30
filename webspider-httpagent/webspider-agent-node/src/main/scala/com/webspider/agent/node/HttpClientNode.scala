package com.webspider.agent.node

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.util.Timeout
import com.webspider.agent.shared.ActorProtocolDefinition
import com.webspider.agent.shared.WebcrawlerProtocol.{ResourceRequest, ResourceResponse}


/**
  * User: Eugene Dzhurinsky
  * Date: 12/24/16
  */
object HttpClientNode {

  private class Worker extends Actor with ActorProtocolDefinition.ConsumerActorTrait {

    override type TaskT = ResourceRequest
    override type ResultT = ResourceResponse
    override type ErrorT = Exception


    import scala.concurrent.duration._

    override def receive: Receive = consumePrototype(Timeout(5 seconds))

    override protected def payloadAction(task: ResourceRequest): Either[Exception, ResourceResponse] = {
      Left(null)
    }
  }

}
