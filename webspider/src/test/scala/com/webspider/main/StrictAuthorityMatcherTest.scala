package com.webspider.main

import filter.StrictAuthorityFilter
import org.specs2.mutable._
import com.webspider.core.HasLink

class StrictAuthorityMatcherTest extends SpecificationWithJUnit {

  val authorityMatcher = new StrictAuthorityFilter() {
    val original: String = "http://ya.ru"
  }

  implicit def string2HasLink(src: String) = {
    new HasLink {
      val link: String = src
    }
  }

  "Matcher of http://ya.ru" should {
    "correctly match authority link http://ya.ru" in {
      authorityMatcher.shallProcess("http://ya.ru") must beTrue
    }
    "correctly match authority link http://ya.ru/" in {
      authorityMatcher.shallProcess("http://ya.ru/") must beTrue
    }
    "correctly match authority link http://ya.ru/context" in {
      authorityMatcher.shallProcess("http://ya.ru/context") must beTrue
    }
    "correctly match authority link http://www.ya.ru/context" in {
      authorityMatcher.shallProcess("http://www.ya.ru/context") must beTrue
    }
    "do not match authority link http://google.com/" in {
      authorityMatcher.shallProcess("http://google.com/") must beFalse
    }
    "do not match authority link http://www.google.com/" in {
      authorityMatcher.shallProcess("http://www.google.com/") must beFalse
    }
  }
}
