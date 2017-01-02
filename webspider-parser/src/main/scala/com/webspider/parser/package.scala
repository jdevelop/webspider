package com.webspider

import org.jsoup.nodes.Element

package object parser {

  def plainAttribute(name: String, buildFrom: String ⇒ TypedResource)(e: Element) = {
    buildFrom {
      e.attr(name)
    }
  }

  val defaults = List(
    ("a[href]", plainAttribute("href", Href.apply) _),
    ("img[src]", plainAttribute("src", Img.apply) _),
    ("script[src]", plainAttribute("src", Script.apply) _),
    ("link[href]", plainAttribute("href", CssLink.apply) _),
    ("form[action]", plainAttribute("action", Form.apply) _),
    ("input[src]", plainAttribute("src", FormInput.apply) _),
    ("embed[src]", plainAttribute("src", Embed.apply) _) //flash embed movies

  )


  sealed trait TypedResource {

    val src: String

  }

  case class Href(src: String) extends TypedResource

  case class Img(src: String) extends TypedResource

  case class Script(src: String) extends TypedResource

  case class CssLink(src: String) extends TypedResource

  case class Form(src: String) extends TypedResource

  case class FormInput(src: String) extends TypedResource

  case class Embed(src: String) extends TypedResource

  type ExtractFunction = Element => TypedResource


  /**
    * Defines methods to be user for document parsing.
    */
  trait DocumentParser[-S] {

    def parse(source: S): Iterable[TypedResource]

  }

}