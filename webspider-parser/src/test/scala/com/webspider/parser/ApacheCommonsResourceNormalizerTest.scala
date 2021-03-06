package com.webspider.parser

import com.webspider.parser.link.ApacheCommonsLinkNormalizer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class ApacheCommonsResourceNormalizerTest extends FunSpec with MustMatchers {

  val normalizer = ApacheCommonsLinkNormalizer

  describe("ApacheCommonsLinkNormalizer") {
    it("should correctly handle url list") {
      io.Source.fromURL(getClass.
        getResource("/url-normalize-list")).
        getLines().
        filter(!_.trim().isEmpty).
        grouped(3).foreach {
        case List(current, raw, formatted) =>
          normalizer.normalize(current, raw) must be(formatted)
        case unknown =>
          fail("Wrong chunk: %1$s".format(unknown))
      }
    }
  }

}
