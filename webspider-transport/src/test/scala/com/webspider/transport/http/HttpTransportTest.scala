package com.webspider.transport.http

import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.transport.DocumentState
import com.webspider.core.Link
import org.specs2.mutable._
import org.apache.http.entity.ContentType

class HttpTransportTest extends SpecificationWithJUnit {

  "HttpTransport" should {
    "be able to access common URLs" in {
      implicit val client = new DefaultHttpClient()

      val (resultStream, state, contentType, headers) = HttpTransport.Get().retrieveDocument(new Link("http://www.google.com"))
      assert(Stream.continually(resultStream.read()).takeWhile(_ != -1).map(_.toByte).toArray.length > 0)
      assert(state === DocumentState.Ok())
      assert(headers.size > 0)
      contentType.map(_.mime) must beSome(ContentType.TEXT_HTML.getMimeType)
    }
  }

}
