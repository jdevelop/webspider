package com.spidersaas.parser

import java.io.InputStream
import com.spidersaas.core.Link
import org.jsoup.Jsoup

import collection.JavaConversions._

class HtmlParser(current: Link,
                 linkListener: LinkListener[Link],
                 expressions: List[(String, ExtractFunction)] = defaults)
  extends DocumentParser[InputStream, Link] {


  override def parse(source: InputStream) {
    val doc = Jsoup.parse(source, "UTF-8", current.link)
    for (
      (exp, attr) <- expressions;
      link <- doc.select(exp)
    ) yield Link(attr(link))
  }

}