package com.webspider.main

import filter.StrictAuthorityFilter
import com.webspider.core.HasLink
import org.junit.runner.RunWith
import org.scalatest.{MustMatchers, FunSpec}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StrictAuthorityMatcherTest extends FunSpec with MustMatchers {

  val authorityMatcher = new StrictAuthorityFilter() {
    val original: String = "http://ya.ru"
  }

  implicit def string2HasLink(src: String): HasLink = {
    new HasLink {
      val link: String = src
    }
  }

  describe("Matcher of http://ya.ru should") {
    it("correctly match authority link http://ya.ru") {
      authorityMatcher.shallProcess("http://ya.ru") must be(true)
    }
    it("correctly match authority link http://ya.ru/") {
      authorityMatcher.shallProcess("http://ya.ru/") must be(true)
    }
    it("correctly match authority link http://ya.ru/context") {
      authorityMatcher.shallProcess("http://ya.ru/context") must be(true)
    }
    it("correctly match authority link http://www.ya.ru/context") {
      authorityMatcher.shallProcess("http://www.ya.ru/context") must be(true)
    }
    it("do not match authority link http://google.com/") {
      authorityMatcher.shallProcess("http://google.com/") must be(false)
    }
    it("do not match authority link http://www.google.com/") {
      authorityMatcher.shallProcess("http://www.google.com/") must be(false)
    }
  }
}
