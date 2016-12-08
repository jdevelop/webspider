package com.webspider.core

/**
 * Injects status code and status message to a entity.
 */
trait ResultState[T] {

  def statusCode: T

  def statusMessage: String

}
