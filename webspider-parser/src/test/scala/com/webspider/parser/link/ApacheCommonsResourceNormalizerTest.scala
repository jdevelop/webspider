package com.webspider.parser.link

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class ApacheCommonsResourceNormalizerTest extends FunSpec with MustMatchers {

  val normalizer = new ApacheCommonsLinkNormalizer

  describe("ApacheCommonsLinkNormalizer") {
    it("should correctly handle url list") {
      io.Source.fromURL(classOf[ApacheCommonsLinkNormalizer].
        getResource("/url-normalize-list")).
        getLines().
        filter(!_.trim().isEmpty).
        grouped(3).foreach {
        case List(current, raw, formatted) =>
          normalizer.normalize(current, raw) must be(Right(formatted))
        case unknown =>
          fail("Wrong chunk: %1$s".format(unknown))
      }
    }
  }

}
