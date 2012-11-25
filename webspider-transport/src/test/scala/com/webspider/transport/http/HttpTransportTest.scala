package com.webspider.transport.http

import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.transport.DocumentState
import com.webspider.core.Link
import org.specs2.mutable._

class HttpTransportTest extends SpecificationWithJUnit {

  "HttpTransport" should {
    "be able to access common URLs" in {
      implicit val client = new DefaultHttpClient()

      val (resultStream, state) = HttpTransport.Get().retrieveDocument(Link("http://www.google.com"))
      assert(Stream.continually(resultStream.read()).takeWhile(_ != -1).map(_.toByte).toArray.length > 0)
      assert(state === DocumentState.Ok())
    }
  }

}
