package com.webspider.storage

import com.webspider.storage.LinkStorage.SaveResult

object LinkStorage {

  sealed trait SaveResult

  case object Add extends SaveResult

  case object Update extends SaveResult

}

trait LinkStorage {

  type LinkType

  def save(link: LinkType): SaveResult

  def results(): Iterable[LinkType]

  def storageSize(): Int
}
