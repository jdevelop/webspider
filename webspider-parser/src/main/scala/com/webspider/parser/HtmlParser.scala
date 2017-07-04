package com.webspider.parser

import java.io.InputStream

import com.webspider.core.TypedResource
import com.webspider.core.utils.LogHelper
import com.webspider.parser.link.RelativeLinkNormalizer
import org.jsoup.Jsoup

import scala.collection.JavaConversions._

object HtmlParser extends LogHelper

abstract class HtmlParser(current: String,
                          expressions: List[(String, ExtractFunction)] = defaults)
  extends DocumentParser[InputStream] {

  val linkNormalizer: RelativeLinkNormalizer

  override def parse(source: InputStream) = {
    val doc = Jsoup.parse(source, "UTF-8", current)
    expressions.flatMap {
      case (cssExpression, extractor) ⇒
        doc.select(cssExpression).map {
          element ⇒ extractor(element)
        }
    } collect {
      case x: TypedResource ⇒
        x
    }
  }

}