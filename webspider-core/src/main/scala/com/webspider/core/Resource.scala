package com.webspider.core

/**
  * Resource entity
  */
case class Resource(location: String,
                    redirectLocation: Option[String] = None,
                    requestHeaders: Map[String, String] = Map(),
                    responseHeaders: Map[String, String] = Map(),
                    statusCode: Int = -1,
                    statusMessage: String = "",
                    contentType: Option[ContentType] = None
                   )
  extends HasLocation
    with HasRedirect
    with HasHTTPHeaders
    with ResultState[Int]
    with HasContentType