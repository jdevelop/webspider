package com.spidersaas.parser

import com.spidersaas.core.Link

trait LinkListener[T <: Link] {

  def linkFound(parent: T, link: T)

}
