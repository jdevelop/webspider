package com.webspider.main.actors

import akka.actor.Actor
import com.webspider.core.utils.LogHelper
import com.webspider.transport.http.HttpTransport
import com.webspider.core.Link
import com.webspider.transport.DocumentState._
import org.apache.http.impl.client.DefaultHttpClient
import com.webspider.parser.{LinkListener, HtmlParser}
import com.webspider.parser.link.RelativeLinkNormalizer
import org.apache.http.HttpStatus
import com.webspider.transport.http.HttpTransport.HttpError
import com.webspider.main.filter.AuthorityMatcher

class Worker(authorityMatcher: AuthorityMatcher,
             relativeLinkNormalizer: RelativeLinkNormalizer) extends Actor with LogHelper {

  def receive = {
    case ProcessLink(link) => {
      try {
        implicit val client = new DefaultHttpClient()
        val (resultStream, state) = HttpTransport.Get().retrieveDocument(link)

        state match {
          case Ok() => {
            link.statusCode = HttpStatus.SC_OK
            sender ! SaveLink(link)

            val needToProcess = authorityMatcher.checkAuthorityMatch(link.link)
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
            link.statusCode = error.errorCode
            link.statusMessage = error.serverReply
            sender ! SaveLink(link)
          }
        }
      } catch {
        case ex: Exception => {
          error("Process of %s ends with exception".format(link.link), ex)
        }
      } finally {
        sender ! LinkProcessingDone(link)
      }
    }
  }
}
