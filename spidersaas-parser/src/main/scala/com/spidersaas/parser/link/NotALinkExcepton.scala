package com.spidersaas.parser.link

case class NotALinkExcepton(reason: String) extends Exception(reason)
