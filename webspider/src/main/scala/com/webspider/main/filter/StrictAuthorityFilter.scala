package com.webspider.main.filter

import com.webspider.core.utils.LogHelper
import org.apache.http.client.utils.URIBuilder
import com.webspider.core.HasLink

abstract class StrictAuthorityFilter extends FilterTrait[HasLink] with LogHelper {

  val original: String

  private val WWW_PART = "www."

  override def shallProcess(target: HasLink): Boolean = {
    try {
      val builder = new URIBuilder(target.link)
      if (!builder.getHost.startsWith(WWW_PART)) {
        builder.setHost(WWW_PART + builder.getHost)
      }
      val originalBuilder = new URIBuilder(original)
      if (!originalBuilder.getHost.startsWith(WWW_PART)) {
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