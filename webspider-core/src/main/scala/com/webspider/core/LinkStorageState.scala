package com.webspider.core

trait LinkStorageState {

  val storageState: LinkStorageState.Value

}

object LinkStorageState extends Enumeration {

  val IN_PROGRESS, QUEUED, PROCESSED, SAVED = Value

}