package com.webspider.parser.link

import com.webspider.core.Link

/**
 * Defines methods to normalize relative links on page.
 */
trait RelativeLinkNormalizer {

  def normalize(current: Link, relativeLink: String): String

}
