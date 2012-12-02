package com.webspider.main

import filter.StrictAuthorityMatcher
import org.specs2.mutable._

class StrictAuthorityMatcherTest extends SpecificationWithJUnit {

  val authorityMatcher = new StrictAuthorityMatcher() {
    val original: String = "http://ya.ru"
  }

  "Matcher of http://ya.ru" should {
    "correctly match authority link http://ya.ru" in {
      authorityMatcher.checkAuthorityMatch("http://ya.ru") must beTrue
    }
    "correctly match authority link http://ya.ru/" in {
      authorityMatcher.checkAuthorityMatch("http://ya.ru/") must beTrue
    }
    "correctly match authority link http://ya.ru/context" in {
      authorityMatcher.checkAuthorityMatch("http://ya.ru/context") must beTrue
    }
    "correctly match authority link http://www.ya.ru/context" in {
      authorityMatcher.checkAuthorityMatch("http://www.ya.ru/context") must beTrue
    }
    "do not match authority link http://google.com/" in {
      authorityMatcher.checkAuthorityMatch("http://google.com/") must beFalse
    }
    "do not match authority link http://www.google.com/" in {
      authorityMatcher.checkAuthorityMatch("http://www.google.com/") must beFalse
    }
  }
}
