package com.webspider.parser

import com.webspider.core.Link

trait LinkListener[T <: Link] {

  def linkFound(parent: T, link: T)

}
