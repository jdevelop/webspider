package com.webspider.core

/**
 * Injects status code and status message to a entity.
 */
trait ResultState[T] {

  private var innerStatusCode: T = _

  private var innerStatusMessage: String = _

  def statusCode = innerStatusCode

  def statusMessage = innerStatusMessage

  def statusCode_=(v: T): Unit = innerStatusCode = v

  def statusMessage_=(v: String): Unit = innerStatusMessage = v

}
