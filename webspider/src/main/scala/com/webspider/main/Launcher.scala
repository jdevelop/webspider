package com.webspider.main

import com.webspider.core.Task
import akka.actor.{Props, ActorSystem}
import com.webspider.main.actors.{ProcessTask, Consumer}
import com.webspider.main.config.TaskConfiguration
import filter.StrictAuthorityFilter


object Launcher {

  def main(args: Array[String]) {
    val url = "http://ya.ru"
    val config = new TaskConfiguration(authorityMatcher = new StrictAuthorityFilter {
      val original: String = url
    })
    processTask(url, config)
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
