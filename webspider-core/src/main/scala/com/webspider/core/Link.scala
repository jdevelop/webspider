package com.webspider.core

import java.util.UUID

import Link.URLT

object Link {

  type URLT = String

}

/**
 * Link entity
 */
case class Link(link: URLT,
                id: UUID,
                redirectLink: Option[URLT] = None,
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