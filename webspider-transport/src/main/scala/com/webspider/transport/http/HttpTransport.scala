package com.webspider.transport.http

import com.webspider.transport.{DocumentState, TransportTrait}
import com.webspider.transport.http.HttpTransport.HttpError
import com.webspider.core.Link
import org.apache.http.client.methods.{HttpGet, HttpRequestBase}
import java.net.URI
import org.apache.http.client.HttpClient
import com.webspider.transport.DocumentState.State

object HttpTransport {

  case class HttpError(errorCode: Int, serverReply: String)

  case class Get[L <: Link](implicit val client: HttpClient)
    extends HttpTransport[L] {

    val method = new HttpGet()

  }

}

/**
 * HTTP connector.
 */
trait HttpTransport[L <: Link] extends TransportTrait[HttpError, L] {

  val method: HttpRequestBase

  implicit val client: HttpClient

  override def retrieveDocument(link: L) = {
    method.setURI(new URI(link.link))
    val response = client.execute(method)
    val statusLine = response.getStatusLine
    val state: State = statusLine.getStatusCode match {
      case 200 => DocumentState.Ok()
      case err => DocumentState.Error(HttpError(statusLine.getStatusCode, statusLine.getReasonPhrase))
    }
    (response.getEntity.getContent, state)
  }

}
