package com.webspider.parser.link

import com.webspider.core.Link
import org.specs2.mutable._

class ApacheCommonsLinkNormalizerTest extends SpecificationWithJUnit {

  val normalizer = new ApacheCommonsLinkNormalizer

  "ApacheCommonsLinkNormalizer" should {
    "correctly handle url list" in {
      io.Source.fromURL(classOf[ApacheCommonsLinkNormalizer].
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
