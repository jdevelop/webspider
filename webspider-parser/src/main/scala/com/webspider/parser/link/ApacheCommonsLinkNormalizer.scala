package com.webspider.parser.link

import com.webspider.core.Link
import org.apache.http.client.utils.{URIUtils}
import java.net.URI
import com.webspider.core.utils.LogHelper

class ApacheCommonsLinkNormalizer extends RelativeLinkNormalizer with LogHelper {

  def normalize(current: Link, relativeLink: String): String = {
    debug("Normalize %s - %s".format(current.link, removeAnchor(relativeLink)))
    URIUtils.resolve(new URI(current.link), removeAnchor(relativeLink)).toString
  }

  def removeAnchor(url: String): String = {
    url.indexOf("#") match {
      case -1 => url
      case pos => url.substring(0, pos)
    }
  }

}
