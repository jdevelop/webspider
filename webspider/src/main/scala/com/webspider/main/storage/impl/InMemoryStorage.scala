package com.webspider.main.storage.impl

import com.webspider.main.storage.Storage
import com.webspider.core.utils.LogHelper
import com.webspider.core.{LinkState, Link}
import collection.mutable.ArrayBuffer
import java.util.UUID

class InMemoryStorage(taskId: Int) extends Storage with LogHelper{

  var links: ArrayBuffer[Link] = new ArrayBuffer[Link]()

  def processed(): Long = links.filter(_.linkState() == LinkState.PROCESSED).size

  def queued(): Long = links.filter(_.linkState() == LinkState.QUEUED).size

  def pop(): Option[Link] = {
    val linkOpt = links.filter(_.linkState() == LinkState.QUEUED).headOption
    linkOpt match {
      case Some(link) => {
        links -= link
        return Some(link)
      }
      case None => None
    }
  }

  def save(link: Link) {
    link.linkState(LinkState.PROCESSED)
    links = links.filterNot(_.uniqueId() == link.uniqueId())
    links += link
  }

  def push(link: Link) {
    link.linkState(LinkState.QUEUED)
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
}

object InMemoryStorageBuilder {
  def builder = new StorageBuilder
  class StorageBuilder {
    private var taskId: Int = 0
    def build(): InMemoryStorage = new InMemoryStorage(this.taskId)
    def withTaskId(id: Int): StorageBuilder = { this.taskId = id; this }
  }
}
