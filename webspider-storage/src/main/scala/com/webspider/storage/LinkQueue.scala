package com.webspider.storage

import com.webspider.core.Link
import com.webspider.storage.LinkQueue.AddResult
import java.util.UUID

object LinkQueue {

  sealed abstract class AddResult

  case object Ok extends AddResult

  case object Retry extends AddResult

}

trait LinkQueue {

  def push(link: Link, parent: UUID): AddResult

  def pop(): Option[Link]

  def reset()

  def queueSize(): Int

}
