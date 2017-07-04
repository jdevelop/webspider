package com.webspider

import com.webspider.core._
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


  type ExtractFunction = Element => TypedResource


  /**
    * Defines methods to be user for document parsing.
    */
  trait DocumentParser[-S] {

    def parse(source: S): Iterable[TypedResource]

  }

}