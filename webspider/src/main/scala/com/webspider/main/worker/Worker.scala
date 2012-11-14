package com.webspider.main.worker

import akka.actor.{ActorRef, Actor}
import com.webspider.core.utils.LogHelper
import com.webspider.transport.http.HttpTransport
import com.webspider.core.Link
import com.webspider.transport.DocumentState._
import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.parser.{LinkListener, HtmlParser}
import com.webspider.parser.link.{SimpleLinkNormalizer, RelativeLinkNormalizer}

class Worker extends Actor with LogHelper {

  def receive = {
    case ProcessLink(link) => {

      implicit val client = new DefaultHttpClient()
      val (resultStream, state) = HttpTransport.Get().retrieveDocument(link)

      state match {
        case Ok() => {
          link.statusCode = 200
          debug("Process link %s %s".format(link, link.statusCode))

          val listener = new LinkListener[Link] {
            def linkFound(parent: Link, child: Link) {
              sender ! StoreLink(parent, child)
            }
          }
          new HtmlParser(link, listener) {
            val linkNormalizer = new SimpleLinkNormalizer
          }.parse(resultStream)
          context.stop(self)
        }
        case Error(_) => {
          debug("Process link %s".format(link) + " returned Error")
        }
      }
    }
  }
}
