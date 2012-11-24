package com.webspider.main.filter

import com.webspider.core.utils.LogHelper
import org.apache.http.client.utils.URIBuilder

abstract class StrictAuthorityMatcher extends AuthorityMatcher with LogHelper{

  val original: String
  private val WWW_PART = "www."

  def checkAuthorityMatch(target: String): Boolean = {
    try {
      val builder = new URIBuilder(target)
      if(!builder.getHost.startsWith(WWW_PART)){
        builder.setHost(WWW_PART + builder.getHost)
      }
      val originalBuilder = new URIBuilder(original)
      if(!originalBuilder.getHost.startsWith(WWW_PART)){
        originalBuilder.setHost(WWW_PART + originalBuilder.getHost)
      }
      return originalBuilder.getHost == builder.getHost
    } catch {
      case e: Exception => {
        error(e, e)
      }
    }
  false
  }
}
