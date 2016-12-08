package com.webspider

import org.jsoup.nodes.Element

package object parser {

  type ExtractFunction = Element => String

  def plainAttribute(name: String)(e: Element) = {
    e.attr(name)
  }

  val defaults = List(
    ("a[href]", plainAttribute("href") _),
    ("img[src]", plainAttribute("src") _),
    ("script[src]", plainAttribute("src") _),
    ("link[href]", plainAttribute("href") _),
    ("input[src]", plainAttribute("src") _),
    ("embed[src]", plainAttribute("src") _), //flash embed movies
    ("form[action]", plainAttribute("action") _)
  )

  abstract case class Document[T](links: List[T])

  /**
    * Defines methods to be user for document parsing.
    */
  trait DocumentParser[-S] {

    type ParserResult

    def parse(source: S): ParserResult

  }

  trait LinkListener {

    def linkFound(link: String)

  }


}
