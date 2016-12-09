package com.webspider.core

/**
 * Defines redirect link.
 */
trait HasRedirect {

  val redirectLocation: Option[String]

}
