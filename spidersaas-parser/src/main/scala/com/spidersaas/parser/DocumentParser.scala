package com.spidersaas.parser

import com.spidersaas.core.Link

/**
 * Defines methods to be user for document parsing.
 */
trait DocumentParser[-S, D <: Link] {

  def parse(source: S)

}
