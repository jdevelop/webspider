package com.webspider.transport

import http.DocumentState
import java.io.InputStream
import com.webspider.core.Link

/**
 * Defines methods to be used for retrieving documents.
 */
trait TransportTrait[T, L <: Link] {

  def retrieveDocument(link: L): (InputStream, DocumentState[T])

}
