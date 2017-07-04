package com.webspider

import java.nio.charset.Charset
import java.util.UUID

/**
  * User: Eugene Dzhurinsky
  * Date: 11/24/16
  */
package object core {

  case class ContentType(mime: String, charset: Option[Charset])

  sealed trait TypedResource {

    val src: String

  }

  case class Href(src: String) extends TypedResource

  case class Img(src: String) extends TypedResource

  case class Script(src: String) extends TypedResource

  case class CssLink(src: String) extends TypedResource

  case class Form(src: String) extends TypedResource

  case class FormInput(src: String) extends TypedResource

  case class Embed(src: String) extends TypedResource

  trait HasContentType {

    val contentType: Option[ContentType]

  }

  trait HasHTTPHeaders {

    val requestHeaders: Map[String, String]

    val responseHeaders: Map[String, String]

  }

  trait HasUniqueId {

    val id: Long

  }

  /**
    * Declares than we do have a link somehow
    */
  trait HasLocation {

    val location: String

  }

  /**
    * Defines redirect link.
    */
  trait HasRedirect {

    val redirectLocation: Option[String]

  }

  /**
    * Injects status code and status message to a entity.
    */
  trait ResultState[T] {

    def statusCode: T

    def statusMessage: String

  }

  case class Resource(location: String,
                      redirectLocation: Option[String] = None,
                      requestHeaders: Map[String, String] = Map(),
                      responseHeaders: Map[String, String] = Map(),
                      statusCode: Int = -1,
                      statusMessage: String = "",
                      contentType: Option[ContentType] = None
                     )
    extends HasLocation
      with HasRedirect
      with HasHTTPHeaders
      with ResultState[Int]
      with HasContentType

  case class Relation(recordId: UUID) {

    private var parents = Set[UUID]()

    private var children = Set[UUID]()

    def addChild(childId: UUID) {
      children += childId
    }

    def addParent(parentId: UUID) {
      parents += parentId
    }

    def getParents = parents

    def getChildren = children

  }


}
