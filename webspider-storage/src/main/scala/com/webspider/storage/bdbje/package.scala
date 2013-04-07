package com.webspider.storage

import com.sleepycat.bind.tuple.TupleBinding
import com.sleepycat.je._
import org.apache.log4j.Logger
import scala.Some

package object bdbje {

  case class SimpleSerializer[T](srz: TupleBinding[T]) {

    def objectToEntry(obj: T): DatabaseEntry = {
      val dbEntry = new DatabaseEntry()
      srz.objectToEntry(obj, dbEntry)
      dbEntry
    }

  }

  implicit def provideSimpleSerializer[T](src: TupleBinding[T]) = SimpleSerializer(src)

  case class SimpleSecondaryCursor(crs: SecondaryCursor) {

    def getFirst(mode: LockMode) = {
      val key = new DatabaseEntry()
      val pKey = new DatabaseEntry()
      val value = new DatabaseEntry()
      crs.getFirst(key, pKey, value, mode) match {
        case OperationStatus.SUCCESS => Some(key, pKey, value)
        case _ => None
      }
    }

  }

  case class SimpleCursor(crs: Cursor) {

    def getFirst(mode: LockMode) = {
      val key = new DatabaseEntry()
      val value = new DatabaseEntry()
      crs.getFirst(key, value, mode) match {
        case OperationStatus.SUCCESS => Some((key, value))
        case _ => None
      }
    }

    def getNext(mode: LockMode) = {
      val key = new DatabaseEntry()
      val value = new DatabaseEntry()
      crs.getNext(key, value, mode) match {
        case OperationStatus.SUCCESS => Some((key, value))
        case _ => None
      }
    }

    def map(lockMode: LockMode)(upd: (DatabaseEntry, DatabaseEntry) => (DatabaseEntry)) {
      val key = new DatabaseEntry()
      val value = new DatabaseEntry()
      while (crs.getNext(key, value, lockMode) == OperationStatus.SUCCESS) {
        crs.putCurrent(upd(key, value))
      }
    }

  }

  implicit def provideSimpleCursor(crs: Cursor) = new SimpleCursor(crs)

  implicit def provideSimpleSecondaryCursor(crs: SecondaryCursor) = new SimpleSecondaryCursor(crs)

  case class QuietCloseable(a: {def close(): Unit}) {
    def closeSilently(implicit LOG: Logger = null) {
      try {
        if (a != null)
          a.close()
      } catch {
        case e: Throwable => Option(LOG).map(_.error(e, e))
      }
    }
  }

  implicit def provideSafeCloseable(a: {def close(): Unit}) = new QuietCloseable(a)

}
