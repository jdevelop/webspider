package com.webspider.core


trait LinkState {
  private var state: Int = _

  def linkState(): Int = state

  def linkState(state: Int) {
    this.state = state
  }
}

object LinkState {
  val IN_PROGRESS = 0
  val QUEUED = 1
  val PROCESSED = 2
  val SAVED = 3
}
