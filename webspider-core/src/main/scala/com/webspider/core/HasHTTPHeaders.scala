package com.webspider.core

/**
 * Holds headers of a link.
 */
trait HasHTTPHeaders {

  val requestHeaders: Map[String, String]

  val responseHeaders: Map[String, String]

}
