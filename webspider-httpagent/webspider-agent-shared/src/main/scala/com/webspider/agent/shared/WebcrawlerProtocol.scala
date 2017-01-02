package com.webspider.agent.shared

/**
  * User: Eugene Dzhurinsky
  * Date: 12/24/16
  *
  * Webcrawler-specific messages
  */
object WebcrawlerProtocol {

  case class ResourceRequest(url: String)

  sealed trait ResponseStatus

  case object ResponseStatusOk extends ResponseStatus

  case class ResponseStatusCode(code: Int, message: String) extends ResponseStatus

  case class ResourceResponse[R](url: String,
                                 finalUrl: String,
                                 response: ResponseStatus,
                                 innerResources: Iterable[R] = Iterable.empty[R]
                                )

}
