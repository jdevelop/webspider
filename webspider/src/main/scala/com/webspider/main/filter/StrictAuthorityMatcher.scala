package com.webspider.main.filter

import com.webspider.core.utils.LogHelper
import java.net.URI
import org.apache.http.client.utils.{URIBuilder}

abstract class StrictAuthorityMatcher extends AuthorityMatcher with LogHelper{

  val original: String

  def checkAuthorityMatch(target: String): Boolean = {
    //debug("Check authority matching " + original + " :: " + target)
    var res = false
    try {

      val u1:URI  = new URIBuilder(original).build()
      val u2:URI  = new URIBuilder(target).build()
      res = (u1.getAuthority() == u2.getAuthority())
      if (!res) {
        var auth1 = u1.getAuthority()
        var auth2 = u2.getAuthority()
        if (!auth1.startsWith("www."))
          auth1 = "www." + auth1
        if (!auth2.startsWith("www."))
          auth2 = "www." + auth2
        res = (auth1 == auth2)
      }
    } catch {
      case e: Exception => error(e)
    }
    return res
  }
}
