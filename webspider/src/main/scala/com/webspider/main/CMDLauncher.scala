package com.webspider.main

import akka.actor.{ActorSystem, Props}
import com.webspider.core.Task
import com.webspider.main.actors.{Consumer, ProcessTask}
import com.webspider.main.config.TaskConfiguration
import com.webspider.main.filter.StrictAuthorityFilter
import org.rogach.scallop.ScallopConf


object CMDLauncher extends App {

  case class CmdArgs(url: String = "", maxWorkers: Int = 35, maxLiks: Int = 500)

  object Conf extends ScallopConf(args) {

    val maxWorkers = opt[Int]("max-workers", noshort = true, default = Some(10))

    val maxLinks = opt[Int]("max-links", noshort = true, default = Some(500))

    val url = trailArg[String]("url")

  }

  val config = new TaskConfiguration(authorityMatcher = new StrictAuthorityFilter {
    val original: String = Conf.url()
  }, maxWorkers = Conf.maxWorkers(), maxLinks = Conf.maxLinks())

  processTask(Conf.url(), config)

  def processTask(url: String, taskConfig: TaskConfiguration) {
    // Create an Akka system
    val system = ActorSystem("SpiderSystem")
    // create the master
    val master = system.actorOf(Props(new Consumer(new Task(url), taskConfig)), name = "consumer")
    // start the calculation
    master ! ProcessTask
  }

}
