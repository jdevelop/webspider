package com.webspider.main.worker

import akka.actor.Actor
import com.webspider.core.utils.LogHelper
import com.webspider.transport.http.HttpTransport
import com.webspider.core.Link
import com.webspider.transport.DocumentState._
import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.parser.{LinkListener, HtmlParser}
import com.webspider.parser.link.SimpleLinkNormalizer
import org.apache.http.HttpStatus

class Worker extends Actor with LogHelper {

  def receive = {
    case ProcessLink(link) => {
      try{
        implicit val client = new DefaultHttpClient()
        val (resultStream, state) = HttpTransport.Get().retrieveDocument(link)

        state match {
          case Ok() => {
            link.statusCode = HttpStatus.SC_OK

            val listener = new LinkListener[Link] {
              def linkFound(parent: Link, child: Link) {
                sender ! StoreLink(parent, child)
              }
            }
            new HtmlParser(link, listener) {
              val linkNormalizer = new SimpleLinkNormalizer
            }.parse(resultStream)
          }
          case Error(errorCode: Int) => {
            link.statusCode = errorCode
            sender ! StoreErrorLink(link)
          }
        }
      }catch {
        case ex: Exception => {
          error("Process of %s ends with exception".format(link.link), ex)
        }
      }finally {
        sender ! LinkProcessingDone(link)
      }
    }
  }
}
