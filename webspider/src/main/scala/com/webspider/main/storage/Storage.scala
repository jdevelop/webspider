package com.webspider.main.storage

import com.webspider.core.Link

trait Storage {
  def init()
  def release()
  def processed(): Long
  def queued(): Long
  def pop(): Option[Link]
  def save(link: Link)
  def push(link: Link)
  def results(): List[Link]
}
