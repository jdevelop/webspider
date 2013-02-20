package com.webspider.main.actors

import akka.actor.Actor
import com.webspider.core.utils.LogHelper
import com.webspider.transport.http.HttpTransport
import com.webspider.core.{ HasLink, Link }
import com.webspider.transport.DocumentState._
import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.parser.{ LinkListener, HtmlParser }
import com.webspider.parser.link.RelativeLinkNormalizer
import org.apache.http.HttpStatus
import com.webspider.transport.http.HttpTransport.HttpError
import com.webspider.main.filter.FilterTrait

class Worker(authorityMatcher: FilterTrait[HasLink],
             relativeLinkNormalizer: RelativeLinkNormalizer) extends Actor with LogHelper {

  def receive = {
    case ProcessLink(link) => {
      try {
        implicit val client = new DefaultHttpClient()
        val (resultStream, state, contentType, responseHeaders) = HttpTransport.Get().retrieveDocument(link)

        state match {
          case Ok() => {
            sender ! SaveLink(link.copy(statusCode = HttpStatus.SC_OK,
              responseHeaders = responseHeaders,
              contentType = contentType))

            val needToProcess = authorityMatcher.shallProcess(link)
            if (needToProcess) {
              val listener = new LinkListener[Link] {
                def linkFound(parent: Link, child: Link) {
                  sender ! AddToNextProcess(parent, child)
                }
              }
              new HtmlParser(link, listener) {
                val linkNormalizer = relativeLinkNormalizer
              }.parse(resultStream)
            }
          }
          case Error(error: HttpError) => {
            sender ! SaveLink(link.copy(statusCode = error.errorCode, statusMessage = error.serverReply))
          }
        }
      } catch {
        case ex: Exception => {
          error("Process of %s ends with exception".format(link.link), ex)
        }
      } finally {
        sender ! LinkProcessingDone(link)
        context.stop(self)
      }
    }
  }
}
