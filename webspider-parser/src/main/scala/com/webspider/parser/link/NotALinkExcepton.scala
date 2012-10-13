package com.webspider.parser.link

case class NotALinkExcepton(reason: String) extends Exception(reason)
