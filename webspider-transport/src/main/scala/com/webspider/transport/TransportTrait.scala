package com.webspider.transport

import java.io.InputStream
import com.webspider.core.{ContentType, Link}
import com.webspider.transport.DocumentState.State

/**
 * Defines methods to be used for retrieving documents.
 */
trait TransportTrait[T, L <: Link] {

  def retrieveDocument(link: L): (InputStream, _ <: State, Option[ContentType], Map[String, String])

}