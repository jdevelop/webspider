package com.webspider.parser

import java.io.InputStream
import com.webspider.core.Link
import link.RelativeLinkNormalizer
import org.jsoup.Jsoup

import collection.JavaConversions._

abstract class HtmlParser(current: Link,
                          linkListener: LinkListener[Link],
                          expressions: List[(String, ExtractFunction)] = defaults)
  extends DocumentParser[InputStream, Link] {

  val linkNormalizer: RelativeLinkNormalizer

  override def parse(source: InputStream) {
    val doc = Jsoup.parse(source, "UTF-8", current.link)
    for (
      (exp, attr) <- expressions;
      link <- doc.select(exp)
    ) {
      linkListener.linkFound(current,Link(attr(link)))
    }
  }

}