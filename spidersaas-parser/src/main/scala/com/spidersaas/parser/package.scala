package com.spidersaas

import org.jsoup.nodes.Element

package object parser {

  type ExtractFunction = Element => String

  def plainAttribute(name: String)(e: Element) = {
    e.attr(name)
  }

  val defaults = List(
    ("a[href]", plainAttribute("href") _),
    ("img[src]", plainAttribute("src") _),
    ("form[action]", plainAttribute("action") _)
  )

}
