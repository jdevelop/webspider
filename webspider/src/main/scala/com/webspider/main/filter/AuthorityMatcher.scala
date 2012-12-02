package com.webspider.main.filter

trait AuthorityMatcher {
  val original: String

  def checkAuthorityMatch(target: String): Boolean
}
