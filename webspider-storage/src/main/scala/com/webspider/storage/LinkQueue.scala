package com.webspider.storage

import com.webspider.core.Link

trait LinkQueue {

  def push(link: Link)

  def pop(): Option[Link]

  def reset()

  def queueSize(): Int

}
