package com.spidersaas.parser

import com.spidersaas.core.Link

abstract case class Document[T <: Link](links: List[T])