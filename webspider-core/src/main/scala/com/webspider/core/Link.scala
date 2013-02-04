package com.webspider.core

import java.util.UUID

/**
 * Link entity
 */
case class Link(link: String,
                id: UUID,
                redirectLink: Option[String] = None,
                requestHeaders: Map[String, String] = Map(),
                responseHeaders: Map[String, String] = Map())
  extends HasLink
  with HasUniqueId
  with RedirectLink
  with Headers
  with ResultState[Int]
  with LinkStorageState {

  def this(lnk: String) = this(lnk, UUID.randomUUID())

}
