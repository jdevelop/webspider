package com.webspider.storage

import com.sleepycat.bind.tuple.TupleBinding
import com.sleepycat.je.{OperationStatus, LockMode, Cursor, DatabaseEntry}

package object bdbje {

  case class SimpleSerializer[T](srz: TupleBinding[T]) {

    def objectToEntry(obj: T): DatabaseEntry = {
      val dbEntry = new DatabaseEntry()
      srz.objectToEntry(obj, dbEntry)
      dbEntry
    }

  }

  implicit def provideSimpleSerializer[T](src: TupleBinding[T]) = SimpleSerializer(src)

  case class SimpleCursor(crs: Cursor) {

    def getFirst(mode: LockMode) = {
      val key = new DatabaseEntry()
      val value = new DatabaseEntry()
      crs.getFirst(key, value, mode) match {
        case OperationStatus.SUCCESS => Some((key, value))
        case _ => None
      }
    }

  }

  implicit def provideSimpleCursor(crs: Cursor) = new SimpleCursor(crs)

}
