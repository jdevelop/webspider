package com.webspider.storage

trait MustInitAndClose {

  def init() = {}

  def close() = {}

}
