package com.webspider.storage

import java.nio.ByteBuffer

import com.sleepycat.bind.tuple.{StringBinding, TupleBinding, TupleInput, TupleOutput}
import com.sleepycat.je._
import org.apache.log4j.Logger

package object bdbje {

  implicit class SimpleSerializer[T](srz: TupleBinding[T]) {

    def writeObjectToEntry(obj: T): DatabaseEntry = {
      val dbEntry = new DatabaseEntry()
      srz.objectToEntry(obj, dbEntry)
      dbEntry
    }

  }

  implicit class SimpleSecondaryCursor(crs: SecondaryCursor) {

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

  implicit class SimpleCursor(crs: Cursor) {

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

  sealed trait StorageClass

  case object Queued extends StorageClass

  case object InProgress extends StorageClass

  private def intToSC(src: Byte) = {
    src match {
      case 1 ⇒ Queued
      case 2 ⇒ InProgress
    }
  }

  private def scToInt(sc: StorageClass) = {
    sc match {
      case Queued ⇒ 1.toByte
      case InProgress ⇒ 2.toByte
    }
  }

  val KeySerializer = new StringBinding

  case class DBEntry(storageClass: StorageClass, priority: Long, key: String, entity: Option[ByteBuffer] = None)

  private class ValueBinder(readFull: Boolean) extends TupleBinding[DBEntry] {
    override def entryToObject(input: TupleInput): DBEntry = {
      val sc = intToSC(input.readByte())
      val queuedAt = input.readLong()
      val key = input.readString()
      val data = if (readFull) {
        val length = input.readInt()
        val arr = ByteBuffer.allocate(length)
        input.read(arr.array(), 0, length)
        arr.position(length)
        arr.flip()
        Some(arr)
      } else {
        None
      }
      DBEntry(sc, queuedAt, key, data)
    }

    override def objectToEntry(obj: DBEntry, output: TupleOutput): Unit = {
      output.writeByte(scToInt(obj.storageClass))
      output.writeLong(obj.priority)
      output.writeString(obj.key)
      val data = obj.entity.get
      output.writeInt(data.limit())
      output.write(data.array(), 0, data.limit())
    }
  }

  val FullDataBinding: TupleBinding[DBEntry] = new ValueBinder(true)

  val PartialDataBinding: TupleBinding[DBEntry] = new ValueBinder(false)

}
