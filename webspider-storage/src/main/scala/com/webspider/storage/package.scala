package com.webspider

import java.nio.ByteBuffer

/**
  * User: Eugene Dzhurinsky
  * Date: 11/29/16
  */
package object storage {

  trait PersistenceSerializer[T] {

    def serialize(src: T): ByteBuffer

    def deserialize(src: ByteBuffer): T

  }

  trait HasStringKey {

    val key: String

  }

}
