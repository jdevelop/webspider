package com.webspider.main.worker

import akka.actor.{Terminated, Props, ActorRef, Actor}
import com.webspider.core.utils.LogHelper
import akka.routing.RoundRobinRouter
import com.webspider.core.{Task, Link}
import com.webspider.main.config.TaskConfiguration
import akka.util.duration._
import com.webspider.main.filter.StrictAuthorityMatcher
import collection.immutable.HashSet

class Master(task: Task, config: TaskConfiguration) extends Actor with LogHelper {

  val MAX_WORKERS = config.maxWorkers
  val SCHEDULER_DELAY = 100 millis
  val storage = config.storage.get
  var workersCount = 0
  var currentProcessingLinks: Set[String] = new HashSet[String]()

  val start: Long = System.currentTimeMillis

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
              debug("Processing the link <%s>".format(link))
              currentProcessingLinks += link.link
              val worker = context.actorOf(Props(new Worker()), name = "worker_%s".format(link.uniqueId()))
              worker ! ProcessLink(link)
              workersCount += 1
            }
            case None => {}
          }
        }
      }
    }

    case LinkProcessingDone(link) => {
      debug("Processing the link <%s> is done".format(link))
      workersCount -= 1
      currentProcessingLinks -= link.link
    }

    case StoreLink(parent, child) => {
      storage.save(parent)
      if (!currentProcessingLinks.contains(child.link)){
        if(StrictAuthorityMatcher.checkAuthorityMatch(task.url, parent.link)){
          storage.push(child)
        }
      }
    }

    case StoreErrorLink(link) => {
      storage.save(link)
    }

    case FinishTask => {
      info("=" * 50)
      info("Finish task %s".format(task))
      info("=" * 50)
      info("Processed : %s".format(storage.processed()))
      info("Queued : %s".format(storage.queued()))
      info("Time consumed : %s ms.".format(System.currentTimeMillis() - start))
      info("=" * 50)
      storage.results().foreach(link => {
        info("%s [%s]".format(link.link, link.statusCode))
      })
      info("=" * 50)
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

  private def processTask(task: Task) = {
    debug("Processing the task %s".format(task))
    storage.init()
    storage.push(Link(task.url))
    context.system.scheduler.schedule(SCHEDULER_DELAY, SCHEDULER_DELAY, self, ProcessQueuedLinks)

    if (config.showStats){
      context.system.scheduler.schedule(SCHEDULER_DELAY, 5 seconds, self, ShowStats)
    }
  }
}
