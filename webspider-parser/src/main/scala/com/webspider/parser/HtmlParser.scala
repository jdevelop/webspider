package com.webspider.parser

import java.io.InputStream

import com.webspider.core.utils.LogHelper
import com.webspider.parser.link.RelativeLinkNormalizer
import org.jsoup.Jsoup

import scala.collection.JavaConversions._

object HtmlParser extends LogHelper

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
      linkNormalizer.normalize(current, attr(link)) match {
        case Left(err) ⇒
          HtmlParser.error(err)
        case Right(resource) ⇒
          linkListener.linkFound(resource)
      }
    }
  }

}