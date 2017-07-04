package com.webspider.agent.shared

import akka.actor.{ActorSystem, AddressFromURIString}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import com.webspider.agent.shared.CLIHelper.ClusterConf
import com.webspider.config.Config

object Bootstrap {

  trait BootstrapNode {

    def configure(conf: ClusterConf, seedNodes: List[String], name: String = "WebspiderCluster"): (Cluster, ActorSystem) = {

      val akkaConf = ConfigFactory.parseString(
        s"""akka.remote.netty.tcp.port=${conf.port}
           |akka.remote.netty.tcp.hostname=${conf.host}
           |akka.remote.netty.tcp.bind-hostname=${conf.bindHost}
           |akka.remote.netty.tcp.bind-port=${conf.bindPort}
           |
      """.stripMargin).withFallback(Config.cfg)

      val actorSystem = ActorSystem(name, akkaConf)

      val c = Cluster(actorSystem)

      if (seedNodes.nonEmpty) {
        c.joinSeedNodes(seedNodes.map(AddressFromURIString.apply))
      }

      c → actorSystem
    }

  }

}
