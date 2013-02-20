package com.webspider.storage

import com.webspider.core.Link
import com.webspider.storage.LinkQueue.{ PopError, AddResult }
import java.util.UUID

object LinkQueue {

  sealed abstract class AddResult

  case object Ok extends AddResult

  case object Retry extends AddResult

  sealed abstract class PopError

  case object AlreadyInProgress extends PopError

  case object NoRecordInDatabase extends PopError

  case class GenericException(e: Throwable) extends PopError

}

trait LinkQueue {

  def push(link: Link, parent: UUID): AddResult

  def pop(): Either[PopError, Link]

  def reset()

  def queueSize(): Long

}
