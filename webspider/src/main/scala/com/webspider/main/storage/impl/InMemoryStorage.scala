package com.webspider.main.storage.impl

import com.webspider.main.storage.Storage
import com.webspider.core.utils.LogHelper
import com.webspider.core.{LinkStorageState, Link}
import java.util.UUID
import collection.mutable.{Set, HashSet}

class InMemoryStorage(taskId: Int) extends Storage with LogHelper {

  var links: Set[Link] = new HashSet[Link]()

  def processed(): Long = links.filter(_.storageState == LinkStorageState.PROCESSED).size

  def queued(): Long = links.filter(_.storageState == LinkStorageState.QUEUED).size

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
    links = links.filterNot(_.uniqueId() == link.uniqueId())
    links += link
  }

  def push(link: Link) {
    link.storageState = LinkStorageState.QUEUED
    link.uniqueId_(UUID.randomUUID())
    links += link
  }

  def init() {
    debug("Init storage")
  }

  def release() {
    debug("Release storage")
    links.clear()
  }

  def results(): List[Link] = links.filter(_.storageState == LinkStorageState.PROCESSED).toList
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
