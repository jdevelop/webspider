package com.webspider.parser

import java.io.InputStream
import com.webspider.core.Resource
import link.RelativeLinkNormalizer
import org.jsoup.Jsoup

import collection.JavaConversions._

abstract class HtmlParser(current: String,
                          linkListener: LinkListener,
                          expressions: List[(String, ExtractFunction)] = defaults)
  extends DocumentParser[InputStream] {


  override type ParserResult = Unit

  val linkNormalizer: RelativeLinkNormalizer

  override def parse(source: InputStream) {
    val doc = Jsoup.parse(source, "UTF-8", current)
    for (
      (exp, attr) <- expressions;
      link <- doc.select(exp)
    ) {
      linkListener.linkFound(linkNormalizer.normalize(current, attr(link)))
    }
  }

}