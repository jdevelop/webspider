package com.webspider.agent.shared

/**
  * User: Eugene Dzhurinsky
  * Date: 12/24/16
  *
  * Webcrawler-specific messages
  */
object WebcrawlerProtocol {

  case class ResourceRequest[T](url: T)

  sealed trait ResponseStatus

  case object ResponseStatusOk extends ResponseStatus

  case class ResponseStatusCode(code: Int, message: String) extends ResponseStatus

  case class ResourceResponse[R](url: R,
                                 finalUrl: String,
                                 response: ResponseStatus,
                                 innerResources: Iterable[R] = Iterable.empty[R]
                                )

}
