package com.spidersaas.parser.link

import com.spidersaas.core.Link

/**
 * Defines methods to normalize relative links on page.
 */
trait RelativeLinkNormalizer {

  def normalize(current: Link, relativeLink: String) : String

}
