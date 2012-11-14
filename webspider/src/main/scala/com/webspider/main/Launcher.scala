package com.webspider.launcher

import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.core.Link
import com.webspider.transport.http.HttpTransport


object Launcher {

  def main(args: Array[String]){
    implicit val client = new DefaultHttpClient()
    val (resultStream, state) = HttpTransport.Get().retrieveDocument(Link("http://ya.ru"))
    println(state)
  }

}
