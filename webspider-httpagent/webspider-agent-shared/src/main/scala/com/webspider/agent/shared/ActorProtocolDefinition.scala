package com.webspider.agent.shared

import akka.actor.{Actor, ActorRef, Terminated}
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import com.webspider.agent.shared.ActorProtocolDefinition.Messages.{NoTask, PullTaskPayload}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * User: Eugene Dzhurinsky
  * Date: 12/12/16
  *
  * Defines the traits aimed to help building pull-task behaviors over Akka.
  *
  */
object ActorProtocolDefinition {

  val ProducerLog = Logger("ActorProtocolDefinition.Producer")

  val ConsumerLog = Logger("ActorProtocolDefinition.Consumer")

  object Messages {

    sealed trait Protocol

    case object ProtocolBeacon

    case class PullTaskRequest(ref: ActorRef) extends Protocol

    case class CancelPullTaskRequest[T](task: T) extends Protocol

    case class PullTaskPayload[T](task: T) extends Protocol

    case class TaskResult[R](result: R) extends Protocol

    case class TaskError[E](error: E) extends Protocol

    case object NoTask extends Protocol

  }

  trait TaskTypesDefinition {

    type TaskT

    type ResultT

    type ErrorT

  }

  trait ProducerActorTrait extends TaskTypesDefinition {

    _: Actor ⇒

    val workers: mutable.Map[ActorRef, TaskT] = new mutable.HashMap()

    private def handleNewTaskRequest(remoteRef: ActorRef) = {
      if (hasMoreTasks) {
        ProducerLog.trace(s"New task request from ${sender}")
        nextTask.foreach {
          task ⇒
            ProducerLog.trace(s"Sending ${task} to ${sender}")
            sender ! PullTaskPayload(task)
            context.watch(remoteRef)
            workers += remoteRef -> task
        }
      } else {
        ProducerLog.trace("No task available")
        sender ! Messages.NoTask
      }

    }

    /**
      * Partial function to handle task processing requests
      */
    def producePrototype: Actor.Receive = {
      case Messages.PullTaskRequest(remoteRef) ⇒
        handleNewTaskRequest(remoteRef)
      case Messages.CancelPullTaskRequest(task: TaskT) ⇒
        ProducerLog.info(s"Task cancelled ${task}")
        workers.get(sender) foreach {
          case existingTask if existingTask == task ⇒
            ProducerLog.info(s"Cancelling existing task from ${sender}")
            context.unwatch(sender)
            workers.remove(sender)
          case _ ⇒
          // do nothing - this worker is busy with something else
        }
        enqueueTask(task)
      case Terminated(ref) ⇒
        ProducerLog.info(s"Actor terminated ${ref}")
        val task = workers.remove(ref)
        task.foreach {
          t ⇒
            ProducerLog.info(s"Enqueue task ${t}")
            enqueueTask(t)
        }
      case (ref: ActorRef, Messages.TaskResult(res: ResultT)) ⇒
        ProducerLog.trace(s"Received response ${res} from $sender")
        processResponse(Right(res))
        ProducerLog.trace(s"Sending beacon to $sender")
        context unwatch ref
        handleNewTaskRequest(ref)
      case (ref: ActorRef,Messages.TaskError(err: ErrorT)) ⇒
        ProducerLog.warn(s"Received error from ${sender} ⇒ $err")
        processResponse(Left(err))
        context unwatch ref
        handleNewTaskRequest(ref)
    }


    // private interface

    protected def hasMoreTasks: Boolean

    protected def nextTask: Option[TaskT]

    protected def enqueueTask(task: TaskT)

    protected def processResponse(src: Either[ErrorT, ResultT])

  }

  trait ConsumerActorTrait extends TaskTypesDefinition {

    _: Actor ⇒

    import akka.pattern._

    def consumePrototype(implicit timeout: Timeout): Actor.Receive = {
      case Messages.ProtocolBeacon ⇒
        ConsumerLog.trace(s"Receive beacon event from ${sender()}")
        context.become(rejectPayloads)
        val memoSender = sender()

        import context.dispatcher

        def handler(src: Try[Messages.Protocol]): Unit = src match {
          case Success(Messages.NoTask) ⇒
            ConsumerLog.info("No task received")
          // okay, no tasks for us
          case Success(pl: Messages.PullTaskPayload[TaskT]) ⇒
            val response: Messages.Protocol = payloadAction(pl.task) match {
              case Right(res: ResultT) ⇒ Messages.TaskResult(res)
              case Left(err: ErrorT) ⇒ Messages.TaskError(err)
            }
            memoSender.?(self -> response).mapTo[Messages.Protocol].onComplete(handler)
          case Failure(e: AskTimeoutException) ⇒
            ConsumerLog.warn(s"Can't receive message from ${memoSender} in time")
          // do nothing
          case Failure(exc: Exception) ⇒
            ConsumerLog.error("Failed to process message data", exc)
          // send some error message back
        }

        val result = (memoSender ? Messages.PullTaskRequest(self))
          .mapTo[Messages.Protocol]

        result.onComplete(handler _ andThen (_ ⇒ context.unbecome()))

    }

    // private interface

    private def rejectPayloads: Actor.Receive = {
      case NoTask ⇒
        ConsumerLog.trace(s"Rejecting notask from ${sender}")
      case pl: Messages.PullTaskPayload[TaskT] ⇒
        ConsumerLog.trace(s"Rejecting payload from ${sender}")
        sender ! Messages.CancelPullTaskRequest(pl.task)
    }

    protected def payloadAction(task: TaskT): Either[ErrorT, ResultT]

  }

}