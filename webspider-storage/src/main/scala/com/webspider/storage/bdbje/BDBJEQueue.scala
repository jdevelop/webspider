package com.webspider.storage.bdbje

import com.webspider.storage.{MustInitAndClose, LinkQueue}
import com.webspider.core.Link

trait BDBJEQueue extends LinkQueue {


  self: MustInitAndClose =>

  def push(link: Link) {

  }

  def pop(): Option[Link] = {
    None
  }

  def reset() {

  }

  def queueSize(): Int = {
    0
  }
}
