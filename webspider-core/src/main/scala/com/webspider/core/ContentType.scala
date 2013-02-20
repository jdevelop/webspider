package com.webspider.core

import java.nio.charset.Charset

case class ContentType(mime: String, charset: Option[Charset])
