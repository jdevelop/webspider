package com.webspider.main.worker

import akka.actor.{Props, ActorRef, Actor}
import com.webspider.core.utils.LogHelper
import akka.routing.RoundRobinRouter
import com.webspider.core.Link
import com.webspider.main.config.TaskConfiguration
import akka.util.duration._

class Master(config: TaskConfiguration) extends Actor with LogHelper {

  val WORKERS = config.maxWorkers
  val SCHEDULER_DELAY = 100 millis
  val storage = config.storage.get

  val start: Long = System.currentTimeMillis
  val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(WORKERS)), name = "workerRouter")

  def receive = {
    case ProcessTask(task) => {
      debug("Processing the task %s".format(task))
      storage.init()
      context.system.scheduler.schedule(SCHEDULER_DELAY, SCHEDULER_DELAY, self, ProcessLinks)
      context.system.scheduler.schedule(SCHEDULER_DELAY, SCHEDULER_DELAY, self, ShowStats)
      context.watch(workerRouter)
      val root = new Link(task.url)
      storage.push(root)
    }
    case StoreLink(parent, child) => {
      debug("Store child link %s of %s".format(child, parent))
      storage.save(parent)
      storage.push(child)
    }
    case ProcessLinks => {
      if (storage.processed() < config.maxLinks){
        storage.pop() match {
          case Some(link) => {
            workerRouter ! ProcessLink(link)
          }
          case None => {}
        }
      }else {
        self ! FinishTask
      }
    }

    case FinishTask => {
      storage.release()
      context.stop(self)
      context.system.shutdown()
    }

    case ShowStats => {
      debug("Processed : %s".format(storage.processed()))
      debug("Queued : %s".format(storage.queued()))
    }

    case _ => {
      debug("Unknown message")
    }
  }

  override def postStop() = {
    debug("Post stop")
  }
}
