package com.webspider.storage.memory

import com.webspider.storage.{MustInitAndClose, LinkQueue, LinkStorage}
import com.webspider.core.utils.LogHelper
import com.webspider.core.{LinkStorageState, Link}
import collection.mutable.{Set, HashSet}
import java.util.UUID

class InMemoryStorage(taskId: Int) extends LinkStorage with LinkQueue with LogHelper with MustInitAndClose {

  var links: Set[Link] = new HashSet[Link]()

  override def storageSize(): Int = links.filter(_.storageState == LinkStorageState.PROCESSED).size

  override def queueSize(): Int = links.filter(_.storageState == LinkStorageState.QUEUED).size

  def pop(): Option[Link] = {
    links.filter(_.storageState == LinkStorageState.QUEUED).headOption.map(link => {
      links -= link
      link.storageState = LinkStorageState.IN_PROGRESS
      links += link
      link
    }).orElse(None)
  }

  def save(link: Link) {
    link.storageState = LinkStorageState.PROCESSED
    links = links.filterNot(_.id == link.id)
    links += link
  }

  def push(link: Link, parent: UUID) = {
    link.storageState = LinkStorageState.QUEUED
    links += link
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
