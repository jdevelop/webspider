package com.webspider.transport.http

import com.webspider.transport.{ DocumentState, TransportTrait }
import com.webspider.transport.http.HttpTransport.HttpError
import com.webspider.core.{ ContentType => ContentType, Link }
import org.apache.http.client.methods.{ HttpGet, HttpRequestBase }
import java.net.URI
import org.apache.http.client.HttpClient
import com.webspider.transport.DocumentState.State
import org.apache.http.{ HttpStatus, HttpHeaders }
import org.apache.http.entity.{ ContentType => ApacheContentType }

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
      case HttpStatus.SC_OK => DocumentState.Ok()
      case err => DocumentState.Error(HttpError(statusLine.getStatusCode, statusLine.getReasonPhrase))
    }
    val headers = response.getAllHeaders.map(header => (header.getName -> header.getValue)).toMap
    val maybeContentType = headers.get(HttpHeaders.CONTENT_TYPE)
      .map(c => apacheContentType2ContentType(ApacheContentType.parse(c)))

    (response.getEntity.getContent, state, maybeContentType, headers)
  }

  private def apacheContentType2ContentType(contentType: ApacheContentType): ContentType = {
    ContentType(contentType.getMimeType, Option(contentType.getCharset))
  }
}
