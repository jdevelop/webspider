package com.webspider.core

/**
 * Holds headers of a link.
 */
trait Headers {

  val requestHeaders: Map[String, String]

  val responseHeaders: Map[String, String]

}
