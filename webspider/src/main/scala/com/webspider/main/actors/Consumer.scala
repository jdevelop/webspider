package com.webspider.main.actors

import akka.actor.{Terminated, Props, Actor}
import com.webspider.core.utils.LogHelper
import com.webspider.core.{Task, Link}
import com.webspider.main.config.TaskConfiguration
import akka.util.duration._
import com.webspider.main.filter.StrictAuthorityMatcher
import com.webspider.main.storage.impl.InMemoryStorageBuilder

class Consumer(task: Task, config: TaskConfiguration) extends Actor with LogHelper {

  val startTime : Long = System.currentTimeMillis

  val MAX_WORKERS = config.maxWorkers
  val SCHEDULER_DELAY = 100 millis
  val storage = config.storage.getOrElse(InMemoryStorageBuilder.builder.withTaskId(1).build())
  var workersCount = 0

  val authorityMatcher = new StrictAuthorityMatcher {
    val original: String = task.url
  }

  def receive = {

    case ProcessTask => {
      processTask(task)
    }

    case ProcessQueuedLinks => {
      if (storage.processed() > config.maxLinks || (storage.queued() == 0 && workersCount == 0)){
        self ! FinishTask
      }else {
        for (i <- 0 until (MAX_WORKERS - workersCount)){
          storage.pop() match {
            case Some(link) => {
              info("Processing the link <%s>".format(link))
              val worker = context.actorOf(Props(new Worker(authorityMatcher)), name = "worker_%s".format(link.uniqueId()))
              worker ! ProcessLink(link)
              workersCount += 1
            }
            case None => {}
          }
        }
      }
    }

    case AddToNextProcess(parent, child) => {
      storage.push(child)
    }

    case SaveLink(link) => {
      storage.save(link)
    }

    case LinkProcessingDone(link) => {
      info("Processing the link <%s> is done".format(link))
      workersCount -= 1
    }

    case FinishTask => {
      showResultsInfo()
      storage.release()
      context.stop(self)
      context.system.shutdown()
    }

    case ShowStats => {
      info("=" * 50)
      info("Stats")
      info("=" * 50)
      info("Processed : %s".format(storage.processed()))
      info("Queued : %s".format(storage.queued()))
      info("Working actors %s".format(workersCount))
      info("=" * 50)
    }

    case Terminated(ref) => {
      debug("Terminated  : " + ref)
    }
  }

  def showResultsInfo() = {
    info("=" * 50)
    info("Finish task %s".format(task))
    info("=" * 50)
    info("Processed : %s".format(storage.processed()))
    info("Queued : %s".format(storage.queued()))
    info("Time consumed : %s ms.".format(System.currentTimeMillis() - startTime))
    info("=" * 50)
    storage.results().foreach(link => {
      info("%s [%s]".format(link.link, link.statusCode))
    })
    info("=" * 50)
  }

  private def processTask(task: Task) = {
    info("Processing the task %s".format(task))
    storage.init()
    storage.push(Link(task.url))
    context.system.scheduler.schedule(SCHEDULER_DELAY, SCHEDULER_DELAY, self, ProcessQueuedLinks)

    if (config.showStats){
      context.system.scheduler.schedule(SCHEDULER_DELAY, 5 seconds, self, ShowStats)
    }
  }
}
