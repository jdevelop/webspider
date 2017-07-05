package com.webspider

import com.webspider.core._
import org.jsoup.nodes.Element

package object parser {

  def plainAttribute(normalizer: String ⇒ String)(name: String, buildFrom: String ⇒ TypedResource)(e: Element) = {
    buildFrom {
      normalizer(e.attr(name))
    }
  }

  def extractorDefaults(normalizer: String ⇒ String) = List(
    ("a[href]", plainAttribute(normalizer)("href", Href.apply) _),
    ("img[src]", plainAttribute(normalizer)("src", Img.apply) _),
    ("script[src]", plainAttribute(normalizer)("src", Script.apply) _),
    ("link[href]", plainAttribute(normalizer)("href", CssLink.apply) _),
    ("form[action]", plainAttribute(normalizer)("action", Form.apply) _),
    ("input[src]", plainAttribute(normalizer)("src", FormInput.apply) _),
    ("embed[src]", plainAttribute(normalizer)("src", Embed.apply) _) //flash embed movies

  )


  type ExtractFunction = Element ⇒ TypedResource


  /**
    * Defines methods to be user for document parsing.
    */
  trait DocumentParser[-S] {

    def parse(source: S): Iterable[TypedResource]

  }

}