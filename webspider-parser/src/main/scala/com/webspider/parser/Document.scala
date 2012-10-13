package com.webspider.parser

import com.webspider.core.Link

abstract case class Document[T <: Link](links: List[T])