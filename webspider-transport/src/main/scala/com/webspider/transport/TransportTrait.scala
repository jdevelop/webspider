package com.webspider.transport

import java.io.InputStream

import com.webspider.core.{ContentType, HasLocation, Resource}

/**
  * Defines methods to be used for retrieving documents.
  */
object TransportTrait {

  type DocumentHandler[ErrorT,R] = Either[ErrorT, DocumentResult] ⇒ R

  case class DocumentResult(is: InputStream, mbContentType: Option[ContentType], headers: Map[String, String])

}

trait TransportTrait {

  type Error

  import TransportTrait._

  def retrieveDocument[R](link: String)(handler: DocumentHandler[Error,R]): R

  def extractErrorCode(err: Error): Int

  def extractErrorMessage(err: Error): String

}