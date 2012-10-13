package com.webspider.core

/**
 * Defines redirect link.
 */
trait RedirectLink {

  private var internalRedirectLnk: Option[String] = None

  def redirectLink() = internalRedirectLnk

  def redirectLink_=(link: String): Unit = this.internalRedirectLnk = Some(link)

}
