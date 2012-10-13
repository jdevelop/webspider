package com.webspider.core

/**
 * Holds headers of a link.
 */
trait Headers {

  var requestHeaders: Map[String, String] = Map()

  var responseHeaders: Map[String, String] = Map()

}
