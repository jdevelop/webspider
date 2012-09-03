package com.spidersaas.core

/**
 * Defines redirect link.
 */
trait HasRedirectLink {

  private var internalRedirectLnk: Option[String] = None

  def redirectLink() = internalRedirectLnk

  def redirectLink_(link: String) {
    this.internalRedirectLnk = Some(link)
  }

}
