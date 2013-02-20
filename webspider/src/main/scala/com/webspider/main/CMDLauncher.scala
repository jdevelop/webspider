package com.webspider.main

import actors.{ ProcessTask, Consumer }
import config.TaskConfiguration
import scopt.immutable.OptionParser
import akka.actor.{ Props, ActorSystem }
import com.webspider.core.Task
import com.webspider.main.filter.StrictAuthorityFilter

object CMDLauncher {

  case class CmdArgs(url: String = "", maxWorkers: Int = 35, maxLiks: Int = 500)

  def main(args: Array[String]) {
    val parser = new OptionParser[CmdArgs]("webspider") {
      def options = Seq(
        opt("u", "url", "url to process") { (v: String, c: CmdArgs) => c.copy(url = v) },
        intOpt("mw", "max-workers", "max worker processes") { (v: Int, c: CmdArgs) => c.copy(maxWorkers = v) },
        intOpt("ml", "max-links", "maximum of processed links") { (v: Int, c: CmdArgs) => c.copy(maxLiks = v) })
    }
    parser.parse(args, CmdArgs()) map { cmdArgs: CmdArgs =>
      if (cmdArgs.url.isEmpty) {
        parser.showUsage
        sys.exit(1)
      }
      val config = new TaskConfiguration(authorityMatcher = new StrictAuthorityFilter {
        val original: String = cmdArgs.url
      }, maxWorkers = cmdArgs.maxWorkers, maxLinks = cmdArgs.maxLiks)

      processTask(cmdArgs.url, config)
    } getOrElse {
      parser.showUsage
    }
  }

  def processTask(url: String, taskConfig: TaskConfiguration) {
    // Create an Akka system
    val system = ActorSystem("SpiderSystem")
    // create the master
    val master = system.actorOf(Props(new Consumer(new Task(url), taskConfig)), name = "consumer")
    // start the calculation
    master ! ProcessTask
  }

}
