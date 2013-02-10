package com.webspider.storage.memory

import com.webspider.storage.{MustInitAndClose, LinkQueue, LinkStorage}
import com.webspider.core.utils.LogHelper
import com.webspider.core.{LinkStorageState, Link}
import collection.mutable.{Set, HashSet}
import java.util.UUID
import com.webspider.storage.LinkQueue.{NoRecordInDatabase, PopError}

class InMemoryStorage(taskId: Int) extends LinkStorage with LinkQueue with LogHelper with MustInitAndClose {

  var links: Set[Link] = new HashSet[Link]()

  override def storageSize(): Int = links.filter(_.storageState == LinkStorageState.PROCESSED).size

  override def queueSize(): Long = links.filter(_.storageState == LinkStorageState.QUEUED).size

  def pop(): Either[PopError, Link] = {
    links.filter(_.storageState == LinkStorageState.QUEUED).headOption.map(link => {
      links -= link
      links += link.copy(storageState = LinkStorageState.IN_PROGRESS)
      Right(link)
    }).getOrElse(Left(NoRecordInDatabase))
  }

  def save(link: Link) {
    links = links.filterNot(_.id == link.id)
    links += link.copy(storageState = LinkStorageState.PROCESSED)
  }

  def push(link: Link, parent: UUID) = {
    links += link.copy(storageState = LinkStorageState.QUEUED)
    LinkQueue.Ok
  }

  def init() {
    debug("Init storage")
  }

  def close() {
    debug("Release storage")
    links.clear()
  }

  def results(): List[Link] = links.filter(_.storageState == LinkStorageState.PROCESSED).toList

  override def reset() {}
}

object InMemoryStorageBuilder {
  def builder = new StorageBuilder

  class StorageBuilder {
    private var taskId: Int = 0

    def build(): InMemoryStorage = new InMemoryStorage(this.taskId)

    def withTaskId(id: Int): StorageBuilder = {
      this.taskId = id
      this
    }
  }

}
