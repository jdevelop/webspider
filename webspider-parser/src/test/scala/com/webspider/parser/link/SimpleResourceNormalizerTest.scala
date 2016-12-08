package com.webspider.parser.link

import com.webspider.core.Resource
import org.junit.runner.RunWith
import org.scalatest.{MustMatchers, FunSpec}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimpleResourceNormalizerTest extends FunSpec with MustMatchers {

  val normalizer = new SimpleLinkNormalizer

  describe("SimpleLinkNormalizer") {
    it("should correctly handle url list") {
      io.Source.fromURL(classOf[SimpleResourceNormalizerTest].
        getResource("/url-normalize-list")).
        getLines().
        filter(!_.trim().isEmpty).
        grouped(3).foreach {
        case List(current, raw, formatted) =>
          normalizer.normalize(current, raw) must be(formatted)
        case unknown => println("Wrong chunk: %1$s".format(unknown))
      }
    }
  }

}
