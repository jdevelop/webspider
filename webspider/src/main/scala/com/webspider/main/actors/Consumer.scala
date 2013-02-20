package com.webspider.main.actors

import akka.actor.{ Terminated, Props, Actor }
import com.webspider.core.utils.LogHelper
import com.webspider.core.{ Task, Link }
import com.webspider.main.config.TaskConfiguration
import scala.concurrent.duration._
import com.webspider.storage.memory.InMemoryStorageBuilder

class Consumer(task: Task, config: TaskConfiguration) extends Actor with LogHelper {

  val startTime: Long = System.currentTimeMillis

  val MAX_WORKERS = config.maxWorkers
  val SCHEDULER_DELAY = 100 millis
  lazy val defaultStorage = InMemoryStorageBuilder.builder.withTaskId(1).build()
  val storage = config.storage.getOrElse(defaultStorage)
  val queue = config.queue.getOrElse(defaultStorage)
  var workersCount = 0

  def receive = {

    case ProcessTask => {
      processTask(task)
    }

    case ProcessQueuedLinks => {
      if (storage.storageSize() > config.maxLinks || (queue.queueSize() == 0 && workersCount == 0)) {
        self ! FinishTask
      } else {
        for (i <- 0 until (MAX_WORKERS - workersCount)) {
          queue.pop() match {
            case Right(link) => {
              info("Processing the link <%s>".format(link))
              val worker = context.actorOf(
                Props(
                  new Worker(config.authorityMatcher, config.linkNormalizer)),
                name = "worker_%s".format(link.id))
              worker ! ProcessLink(link)
              workersCount += 1
            }
            // FIXME should process with retry or abort here
            case Left(_) => {}
          }
        }
      }
    }

    case AddToNextProcess(parent, child) => {
      queue.push(child, parent.id)
    }

    case SaveLink(link) => {
      storage.save(link)
    }

    case LinkProcessingDone(link) => {
      workersCount -= 1
    }

    case FinishTask => {
      showResultsInfo()
      context.stop(self)
      context.system.shutdown()
    }

    case ShowStats => {
      logSeparator
      info("Stats")
      logSeparator
      info("Processed : %s".format(storage.storageSize()))
      info("Queued : %s".format(queue.queueSize()))
      info("Working actors %s".format(workersCount))
      logSeparator
    }

    case Terminated(ref) => {
      debug("Terminated  : " + ref)
    }
  }

  def showResultsInfo() {
    logSeparator
    info("Finish task %s".format(task))
    logSeparator
    info("Processed : %s".format(storage.storageSize()))
    info("Queued : %s".format(queue.queueSize()))
    info("Time consumed : %s ms.".format(System.currentTimeMillis() - startTime))
    logSeparator
    storage.results().foreach(link => {
      info("%s [%s] [%s] ".format(link.link, link.contentType, link.statusCode))
    })
    logSeparator
  }

  private def processTask(task: Task) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    info("Processing the task %s".format(task))
    queue.push(new Link(task.url), null)
    context.system.scheduler.schedule(SCHEDULER_DELAY, SCHEDULER_DELAY, self, ProcessQueuedLinks)

    if (config.showStats) {
      context.system.scheduler.schedule(SCHEDULER_DELAY, 5 seconds, self, ShowStats)
    }
  }
}
