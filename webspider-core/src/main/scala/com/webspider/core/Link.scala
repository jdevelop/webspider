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
                responseHeaders: Map[String, String] = Map(),
                storageState: LinkStorageState.Value = LinkStorageState.QUEUED,
                statusCode: Int = -1,
                statusMessage: String = "",
                contentType: Option[ContentType] = None
                )
  extends HasLink
  with HasUniqueId
  with RedirectLink
  with Headers
  with ResultState[Int]
  with LinkStorageState
  with HasContentType{

  def this(lnk: String) = this(lnk, UUID.randomUUID())

}