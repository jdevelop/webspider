package com.webspider.transport.http

import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.transport.DocumentState
import com.webspider.core.Link
import org.specs2.mutable._
import org.apache.http.entity.ContentType

class HttpTransportTest extends SpecificationWithJUnit {

   "HttpTransport" should {
    "be able to access common URLs" in {
      Thread.sleep(300)
      "ex1".pp
      ok
    }
    "be able to access common URLs --- 2" in {
      "ex2".pp
      ok
    }
  }


}
