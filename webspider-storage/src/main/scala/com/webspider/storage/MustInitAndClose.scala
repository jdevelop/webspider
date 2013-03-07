package com.webspider.storage

trait MustInitAndClose[T] {

  def init() = {}

  def close(f: T => Unit) = {}

}
