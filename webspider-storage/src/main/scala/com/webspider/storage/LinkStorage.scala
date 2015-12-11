package com.webspider.storage

import com.webspider.core.Link
import com.webspider.storage.LinkStorage.SaveResult

object LinkStorage {

  sealed trait SaveResult

  case object Add extends SaveResult

  case object Update extends SaveResult

}

trait LinkStorage {

  def save(link: Link) : SaveResult

  def results(): Iterable[Link]

  def storageSize(): Int
}
