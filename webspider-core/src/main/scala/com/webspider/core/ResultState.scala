package com.webspider.core

/**
 * Injects status code and status message to a entity.
 */
trait ResultState[T] {
  val statusCode: T
  val statusMessage: String
}
