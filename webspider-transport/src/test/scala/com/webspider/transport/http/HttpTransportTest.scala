package com.webspider.transport.http

import com.webspider.transport.TransportTrait.DocumentResult
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.DefaultHttpClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class HttpTransportTest extends FunSpec with ShouldMatchers {

  describe("HttpTransport") {
    it("should be able to access common URLs") {
      implicit val client = new DefaultHttpClient()

      HttpTransport.Get(client).retrieveDocument("http://www.google.com") {
        case Right(DocumentResult(resultStream, contentType, headers)) ⇒
          Stream.continually(resultStream.read()).takeWhile(_ != -1).map(_.toByte).toArray.length should be > 0
          headers.nonEmpty should be(true)
          contentType.map(_.mime) should be(Some(ContentType.TEXT_HTML.getMimeType))
        case _ ⇒ fail()
      }
    }
  }

}
