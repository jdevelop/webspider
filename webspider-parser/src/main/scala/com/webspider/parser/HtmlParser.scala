package com.webspider.parser

import java.io.InputStream

import com.webspider.core.TypedResource
import org.jsoup.Jsoup

import scala.collection.JavaConversions._

object HtmlParser

case class HtmlParser(current: String,
                      expressions: List[(String, ExtractFunction)])
  extends DocumentParser[InputStream] {

  override def parse(source: InputStream) = {
    val doc = Jsoup.parse(source, "UTF-8", current)
    expressions.flatMap {
      case (cssExpression, extractor) ⇒
        doc.select(cssExpression).map {
          element ⇒ extractor(element)
        }
    } collect {
      case x: TypedResource ⇒ x
    }
  }

}