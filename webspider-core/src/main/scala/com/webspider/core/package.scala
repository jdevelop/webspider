package com.webspider

import java.nio.charset.Charset

/**
  * User: Eugene Dzhurinsky
  * Date: 11/24/16
  */
package object core {

  case class ContentType (mime: String, charset: Option[Charset])


}
