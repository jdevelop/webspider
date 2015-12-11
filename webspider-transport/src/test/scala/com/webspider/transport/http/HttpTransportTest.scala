package com.webspider.transport.http

import com.webspider.core.Link
import com.webspider.transport.DocumentState
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.DefaultHttpClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{ShouldMatchers, FunSpec}

@RunWith(classOf[JUnitRunner])
class HttpTransportTest extends FunSpec with ShouldMatchers {

  describe("HttpTransport") {
    it("should be able to access common URLs") {
      implicit val client = new DefaultHttpClient()

      val (resultStream, state, contentType, headers) = HttpTransport.Get().retrieveDocument(new Link("http://www.google.com"))
      assert(Stream.continually(resultStream.read()).takeWhile(_ != -1).map(_.toByte).toArray.length > 0)
      assert(state === DocumentState.Ok())
      assert(headers.size > 0)
      contentType.map(_.mime) should be(Some(ContentType.TEXT_HTML.getMimeType))
    }
  }

}
