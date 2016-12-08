package com.webspider.transport

import java.io.InputStream

import com.webspider.core.{ContentType, HasLocation, Resource}

/**
  * Defines methods to be used for retrieving documents.
  */
object TransportTrait {

  type DocumentHandler[ErrorT] = Either[ErrorT, DocumentResult] ⇒ Unit

  case class DocumentResult(is: InputStream, mbContentType: Option[ContentType], headers: Map[String, String])

}

trait TransportTrait {

  type Error

  import TransportTrait._

  def retrieveDocument(link: HasLocation)(handler: DocumentHandler[Error]): Unit

  def extractErrorCode(err: Error): Int

  def extractErrorMessage(err: Error): String

}