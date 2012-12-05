package com.webspider.core

/**
 * Link entity
 */
case class Link(link: String)
  extends HasLink
  with HasUniqueId
  with RedirectLink
  with Headers
  with ResultState[Int]
  with LinkStorageState
