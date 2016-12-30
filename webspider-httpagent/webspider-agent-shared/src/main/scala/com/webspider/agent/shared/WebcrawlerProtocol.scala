package com.webspider.agent.shared

/**
  * User: Eugene Dzhurinsky
  * Date: 12/24/16
  *
  * Webcrawler-specific messages
  */
object WebcrawlerProtocol {

  case class ResourceRequest(url: String)

  case class ResponseStatus(code: Int, message: String)

  case class ResourceResponse(url: String, finalUrl: String, response: ResponseStatus)

}
