package com.webspider.parser.link

import com.webspider.core.Link
import org.junit.runner.RunWith
import org.scalatest.{MustMatchers, FunSpec}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ApacheCommonsLinkNormalizerTest extends FunSpec with MustMatchers {

  val normalizer = new ApacheCommonsLinkNormalizer

  describe("ApacheCommonsLinkNormalizer") {
    it("should correctly handle url list") {
      io.Source.fromURL(classOf[ApacheCommonsLinkNormalizer].
        getResource("/url-normalize-list")).
        getLines().
        filter(!_.trim().isEmpty).
        grouped(3).foreach {
        case List(current, raw, formatted) =>
          normalizer.normalize(new Link(current), raw) must be(formatted)
        case unknown => println("Wrong chunk: %1$s".format(unknown))
      }
    }
  }

}
