package com.webspider.storage

import com.webspider.core.Link

trait LinkStorage {

  def save(link: Link)

  def results(): Iterable[Link]

  def storageSize(): Int
}
