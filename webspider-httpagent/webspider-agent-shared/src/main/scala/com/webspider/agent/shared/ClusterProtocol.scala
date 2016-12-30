package com.webspider.agent.shared

import akka.actor.{Actor, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Put, Subscribe, SubscribeAck}

/**
  * User: Eugene Dzhurinsky
  * Date: 12/27/16
  *
  * Akka-cluster related methods and definitions.
  *
  */
object ClusterProtocol {

  protected[ClusterProtocol] trait ClusterCommon {

    clusterAware: Actor ⇒

    protected lazy val mediator: ActorRef = DistributedPubSub(context.system).mediator

  }

  trait ClusterSubscriber extends ClusterCommon {

    clusterAware: Actor ⇒

    protected def TopicName: String

    protected def register(): Unit = {
      mediator ! Subscribe(TopicName, self)
      context become awaitClusterRegistration
    }

    private def awaitClusterRegistration: Actor.Receive = {
      case SubscribeAck(Subscribe(tName, _, `self`)) if tName == TopicName ⇒
        context.unbecome()
        onSubscribeDone()
    }

    def onSubscribeDone(): Unit = {}

  }

  trait ClusterPublisher extends ClusterCommon {

    clusterAware: Actor ⇒

    def publish(msg: Any, topic: String): Unit = {
      mediator ! Publish(topic, msg)
    }

  }

}