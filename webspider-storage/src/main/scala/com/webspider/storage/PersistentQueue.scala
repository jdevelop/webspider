package com.webspider.storage

import com.webspider.storage.PersistentQueue.{AddResult, PopError}

object PersistentQueue {

  sealed trait AddResult

  case object Added extends AddResult

  case object Retry extends AddResult

  sealed abstract class PopError

  case object AlreadyInProgress extends PopError

  case object NoRecordInDatabase extends PopError

  case class GenericException(e: Throwable) extends PopError

}

trait PersistentQueue {

  type QueueRecord

  def push(link: QueueRecord): AddResult

  def pop(): Either[PopError, QueueRecord]

  def reset()

  def queueSize(): Long

}