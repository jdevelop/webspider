package com.webspider.transport.http

import java.io.InputStream
import java.net.URI

import com.webspider.core.{ContentType, HasLocation, Resource}
import com.webspider.transport.TransportTrait
import com.webspider.transport.TransportTrait.DocumentResult
import com.webspider.transport.http.HttpTransport.HttpError
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.{HttpGet, HttpRequestBase}
import org.apache.http.entity.{ContentType => ApacheContentType}
import org.apache.http.{HttpHeaders, HttpStatus}

object HttpTransport {

  case class HttpError(errorCode: Int, serverReply: String)

  case class Get[L <: Resource](client: HttpClient) extends HttpTransport {

    val method = new HttpGet()

  }

}

/**
  * HTTP connector.
  */
trait HttpTransport extends TransportTrait {

  override type Error = HttpTransport.HttpError

  def method: HttpRequestBase

  def client: HttpClient

  override def retrieveDocument[R](link: String)(handler: TransportTrait.DocumentHandler[Error,R]) = {
    var content: InputStream = null
    try {
      method.setURI(new URI(link))
      val response = client.execute(method)
      val statusLine = response.getStatusLine
      val res = handler {
        statusLine.getStatusCode match {
          case HttpStatus.SC_OK =>
            val headers = response.getAllHeaders.map(header ⇒ header.getName -> header.getValue).toMap
            val maybeContentType = headers.get(HttpHeaders.CONTENT_TYPE)
              .map(c => apacheContentType2ContentType(ApacheContentType.parse(c)))
            content = response.getEntity.getContent
            Right(DocumentResult(content, maybeContentType, headers))
          case err =>
            Left(HttpError(statusLine.getStatusCode, statusLine.getReasonPhrase))
        }
      }
      content.close()
      res
    } finally {
      method.releaseConnection()
    }
  }

  override def extractErrorCode(err: HttpError): Int = err.errorCode

  override def extractErrorMessage(err: HttpError): String = err.serverReply

  private def apacheContentType2ContentType(contentType: ApacheContentType): ContentType = {
    ContentType(contentType.getMimeType, Option(contentType.getCharset))
  }
}