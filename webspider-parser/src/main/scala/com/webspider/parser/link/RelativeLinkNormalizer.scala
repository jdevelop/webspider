package com.webspider.parser.link

/**
  * Defines methods to normalize relative links on page.
  */
trait RelativeLinkNormalizer {

  def normalize(current: String, relativeLink: String): Either[String, String]

}
