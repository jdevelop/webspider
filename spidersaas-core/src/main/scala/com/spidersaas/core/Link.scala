package com.spidersaas.core

/**
 * Link entity
 */
case class Link(link: String)
  extends HasUniqueId
  with RedirectLink
  with Headers
  with ResultState[Int]
