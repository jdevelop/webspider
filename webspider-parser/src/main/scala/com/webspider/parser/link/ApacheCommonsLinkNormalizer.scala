package com.webspider.parser.link

import java.net.URI

import org.apache.http.client.utils.URIUtils

object ApacheCommonsLinkNormalizer extends RelativeLinkNormalizer {

  def normalize(current: String, relativeLink: String): String = {
    URIUtils.resolve(new URI(current), removeAnchor(relativeLink)).toString
  }

  def removeAnchor(url: String): String = {
    url.indexOf("#") match {
      case -1 => url
      case pos => url.substring(0, pos)
    }
  }

}
