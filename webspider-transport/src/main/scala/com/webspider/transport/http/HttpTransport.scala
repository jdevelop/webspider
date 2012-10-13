package com.webspider.transport.http

import com.webspider.transport.TransportTrait
import com.webspider.transport.http.HttpTransport.HttpError
import com.webspider.core.Link

object HttpTransport {

  case class HttpError(errorCode: Int, serverReply: String)

}

/**
 * HTTP connector.
 */
class HttpTransport[L <: Link] extends TransportTrait[HttpError, L] {

  override def retrieveDocument(link: L) = (null, null)

}
