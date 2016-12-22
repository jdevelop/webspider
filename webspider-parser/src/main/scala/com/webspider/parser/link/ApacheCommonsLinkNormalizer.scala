package com.webspider.parser.link

import java.net.URI

import com.webspider.core.utils.LogHelper
import org.apache.http.client.utils.URIUtils

class ApacheCommonsLinkNormalizer extends RelativeLinkNormalizer with LogHelper {

  def normalize(current: String, relativeLink: String): Either[String, String] = {
    debug("Normalize %s - %s".format(current, removeAnchor(relativeLink)))
    Right(URIUtils.resolve(new URI(current), removeAnchor(relativeLink)).toString)
  }

  def removeAnchor(url: String): String = {
    url.indexOf("#") match {
      case -1 => url
      case pos => url.substring(0, pos)
    }
  }

}
