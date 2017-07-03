package com.webspider.scheduler.node

import com.webspider.agent.shared.CLIHelper
import org.rogach.scallop.ScallopConf

object TaskSchedulerNode {

  def main(args: Array[String]): Unit = {

    object Conf extends ScallopConf(args) with CLIHelper.ClusterParams with CLIHelper.ErrorReporter

    Conf.verify()

  }

  def startScheduler(): Unit = {

  }

}
