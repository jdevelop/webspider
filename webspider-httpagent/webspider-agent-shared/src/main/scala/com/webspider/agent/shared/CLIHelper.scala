package com.webspider.agent.shared

import org.rogach.scallop.ScallopConf
import org.rogach.scallop.exceptions.ScallopException

object CLIHelper {

  case class ClusterConf(host: String, bindHost: String, port: Int, bindPort: Int)

  trait ClusterParams {

    _: ScallopConf ⇒

    val seedNodes = opt[List[String]]("seed-nodes")

    val clusterConf = opt[ClusterConf]("cluster", descr = "host:port[:bindhost[:bindport]]", required = true)(org.rogach.scallop.singleArgConverter {
      str ⇒
        str.split(":") match {
          case Array(host, port) ⇒
            val portInt = port.toInt
            ClusterConf(host, host, portInt, portInt)
          case Array(host, port, bindHost) ⇒
            val portInt = port.toInt
            ClusterConf(host, bindHost, portInt, portInt)
          case Array(host, port, bindHost, bindPort) ⇒
            val portInt = port.toInt
            val bindPortInt = bindPort.toInt
            ClusterConf(host, bindHost, portInt, bindPortInt)
        }
    })


  }

  trait ErrorReporter {

    _: ScallopConf ⇒

    override protected def onError(e: Throwable): Unit = e match {
      case err: ScallopException ⇒
        Console.err.println(err.getMessage)
        printHelp()
        sys.exit(1)
      case err: Exception ⇒ throw err
    }


  }

}
