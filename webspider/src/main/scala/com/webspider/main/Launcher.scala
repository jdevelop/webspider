package com.webspider.launcher

import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.core.{Task, Link}
import com.webspider.transport.http.HttpTransport
import akka.actor.{Props, ActorSystem}
import com.webspider.main.worker.{ProcessTask, Master}
import com.webspider.main.config.TaskConfiguration
import com.webspider.main.storage.impl.InMemoryStorageBuilder


object Launcher {

  def main(args: Array[String]){
    val config = new TaskConfiguration()
    config.storage = Some(InMemoryStorageBuilder.builder.withTaskId(1).build())
    processTask("http://google.com", config)
  }

  def processTask(url: String, taskConfig: TaskConfiguration){
    // Create an Akka system
    val system = ActorSystem("SpiderSystem")

    // create the master
    val master = system.actorOf(Props(new Master(new Task(url), taskConfig)), name = "master")

    // start the calculation
    master ! ProcessTask
  }
}
