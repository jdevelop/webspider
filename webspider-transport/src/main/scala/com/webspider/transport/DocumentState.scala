package com.webspider.transport

object DocumentState {

  sealed case class State()

  case class Ok() extends State

  case class Error[T](error: T) extends State

}

/**
 * Holds document state
 */
abstract case class DocumentState[T]() {
  val state: DocumentState[T]
}
