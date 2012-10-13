package com.webspider.transport

object DocumentState {

  sealed case class State()

  case class Ok() extends State

  case class Error[T](error: T) extends State

}