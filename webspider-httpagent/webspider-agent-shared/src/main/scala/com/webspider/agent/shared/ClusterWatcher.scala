package com.webspider.agent.shared

import akka.actor.{Actor, ActorSystem, Props}
import akka.cluster.Cluster
import akka.remote.ThisActorSystemQuarantinedEvent
import com.typesafe.scalalogging.Logger

object ClusterWatcher {

  private val LOG = Logger(getClass)

  private class DefaultClusterWatcher extends Actor with ClusterWatcher {
    override def receive = handleQuarantinedRestart
  }

  def registerRestartJVMWatcherActor(sys: ActorSystem) = {
    val ref = sys.actorOf(Props[DefaultClusterWatcher])
    sys.eventStream.subscribe(ref, classOf[ThisActorSystemQuarantinedEvent])
  }

  def onRemovedEvent(c: Cluster): Unit = {
    c.registerOnMemberRemoved {
      if (c.isTerminated) {
        LOG.error(s"Terminate cluster ${c.selfAddress}")
        sys.exit(1)
      } else {
        LOG.warn(s"Member was terminated but actually not: ${c.selfAddress}")
      }
    }
  }

}

trait ClusterWatcher {

  _: Actor ⇒

  def handleQuarantinedRestart: Actor.Receive = {
    case err: ThisActorSystemQuarantinedEvent ⇒
      sys.exit(1)
  }

}
