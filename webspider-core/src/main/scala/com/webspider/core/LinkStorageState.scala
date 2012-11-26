package com.webspider.core


trait LinkStorageState {
  private var state: Int = _

  def storageState = state

  def storageState_=(state: Int) {
    this.state = state
  }
}

object LinkStorageState {
  val IN_PROGRESS = 0
  val QUEUED = 1
  val PROCESSED = 2
  val SAVED = 3
}
