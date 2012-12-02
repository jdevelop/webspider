package com.webspider.parser.link

import com.webspider.core.Link
import org.specs2.mutable._

class SimpleLinkNormalizerTest extends SpecificationWithJUnit {

  val normalizer = new SimpleLinkNormalizer

  "SimpleLinkNormalizer" should {
    "correctly handle url list" in {
      io.Source.fromURL(classOf[SimpleLinkNormalizerTest].
        getResource("/url-normalize-list")).
        getLines().
        filter(!_.trim().isEmpty).
        grouped(3).foreach {
        case List(current, raw, formatted) =>
          normalizer.normalize(new Link(current), raw) must equalTo(formatted)
        case unknown => println("Wrong chunk: %1$s".format(unknown))
      }
    }
  }

}
