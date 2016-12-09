package com.webspider.storage.bdbje

import java.util.concurrent.atomic.AtomicLong

import com.sleepycat.bind.tuple.{LongBinding, StringBinding}
import com.sleepycat.je._
import com.webspider.storage.PersistentQueue.{AlreadyInProgress, GenericException, NoRecordInDatabase, PopError}
import com.webspider.storage.{HasStringKey, MustInitAndClose, PersistentQueue}
import grizzled.slf4j.Logger

object BDBJEQueue {

  val LOG = Logger(getClass)

}

import com.webspider.storage.bdbje.BDBJEQueue.LOG

trait BDBJEQueue[T <: HasStringKey] extends PersistentQueue with MustInitAndClose[Environment] {

  import com.webspider.storage.PersistenceSerializer

  override type QueueRecord = T

  protected val serializer: PersistenceSerializer[QueueRecord]

  val qSize = new AtomicLong(0)

  protected def env: Environment

  // Url -> Record
  protected def urlDatabase: Database

  // Contains mappings record ID -> parent ID
  protected def relationDatabase: Database

  // holds queue of links
  protected def queueDatabase: SecondaryDatabase

  // holds current "in-progress" links
  protected def inprogressDatabase: SecondaryDatabase

  protected val linkKeySerializer = new StringBinding()

  protected val idSerializer = new LongBinding

  private[this] def dump(e: DatabaseEntry) = {
    e.getSize.toString + "=>" + e.getData.map("%02X" format _).mkString
  }

  override def push(link: QueueRecord) = {
    val data = serializer.serialize(link)

    val txn = env.beginTransaction(null, null)
    val cursor = urlDatabase.openCursor(txn, null)
    var complete = false
    try {
      val keyRec = linkKeySerializer.writeObjectToEntry(link.key)
      val urlRec = FullDataBinding.writeObjectToEntry(DBEntry(Queued, System.currentTimeMillis(), link.key, Some(data)))
      // check if link is in the queue or was processed either as redirect link or as regular one
      LOG.debug("Pushing record '" + link.key + "'")

      val opResult = cursor.putNoOverwrite(keyRec, urlRec) match {
        case OperationStatus.KEYEXIST => // if yes - then update parent link relation and that's it
          LOG.debug("Link exists '" + link.key + "'")
          PersistentQueue.Added
        case OperationStatus.SUCCESS => // if no - then add link to the database
          qSize.incrementAndGet()
          PersistentQueue.Added
        case status ⇒
          LOG.error(s"Can't process operation status ${status}")
          PersistentQueue.Retry
      }

      complete = true
      opResult
    } finally {
      cursor.closeSilently
      if (complete) {
        txn.commitNoSync()
      } else {
        txn.abort()
      }
    }
  }

  override def pop(): Either[PopError, QueueRecord] = {
    // take first link which from the queue
    implicit def exception = (e: Throwable) => Left(GenericException(e))

    withCursorInTransaction(queueDatabase) {
      case (cursor, txn) => cursor.getFirst(LockMode.RMW).map {
        case (key, pKey, value) =>
          val entry = FullDataBinding.entryToObject(value)
          LOG.debug("KEY: " + dump(pKey))
          // update it's state to "in progress"
          entry.storageClass match {
            case Queued ⇒
              val newEntry = entry.copy(storageClass = InProgress)
              urlDatabase.put(txn, pKey, FullDataBinding.writeObjectToEntry(newEntry))
              Right(serializer.deserialize(newEntry.entity.get))
            case InProgress =>
              Left(AlreadyInProgress)
          }
      }.getOrElse(Left(NoRecordInDatabase))
    }
  }

  override def reset() {
    // mark all links "in progress" as queued.
    implicit def exception = (e: Throwable) => Left(e)

    withCursorInTransaction(inprogressDatabase) {
      case (cursor, txn) =>
        cursor.map(LockMode.RMW) {
          case (key, value) =>
            val entry = FullDataBinding.entryToObject(value)
            FullDataBinding.objectToEntry(entry.copy(storageClass = Queued), value)
            value
        }
        Right(())
    }
  }

  override def queueSize(): Long = {
    qSize.get()
  }

  private def withCursorInTransaction[E, A](db: SecondaryDatabase)
                                           (block: (SecondaryCursor, Transaction) => Either[E, A])
                                           (implicit exception: Throwable => Either[E, A]) = {
    import BDBJEQueue.LOG
    val txn = env.beginTransaction(null, null)
    val cursor = db.openCursor(txn, null)
    var result: Either[E, A] = null
    try {
      result = block(cursor, txn)
    } catch {
      case e: Exception => LOG.error("Error in transaction", e); exception(e)
    } finally {
      cursor.closeSilently
      Option(txn).foreach {
        _ =>
          result match {
            case Left(_) => txn.abort()
            case Right(_) => txn.commit()
          }
      }
    }
    result
  }

  abstract override def init() {
    super.init()
    qSize.set(queueDatabase.count())
  }
}