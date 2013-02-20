package com.webspider.storage.bdbje

import com.webspider.storage.{ MustInitAndClose, LinkQueue }
import com.webspider.core.{ LinkStorageState, Link }
import com.sleepycat.bind.tuple.TupleBinding
import java.util.UUID
import com.sleepycat.je._

import Link.URLT
import com.webspider.storage.LinkQueue.{ GenericException, NoRecordInDatabase, AlreadyInProgress, PopError }
import org.apache.log4j.Logger
import java.util.concurrent.atomic.AtomicLong

object BDBJEQueue {

  implicit private val LOG: Logger = Logger.getLogger(classOf[BDBJEQueue])

}

trait BDBJEQueue extends LinkQueue with MustInitAndClose {

  val qSize = new AtomicLong(0)

  def env: Environment

  // contains UUID -> Record mappings
  def mainDatabase: Database

  // Containts redirect URL -> UUID mapping
  def urlDatabase: Database

  // Contains mappings UUID -> parent UUID
  def relationDatabase: Database

  // holds queue of links
  def queueDatabase: SecondaryDatabase

  // holds current "in-progress" links
  def inprogressDatabase: SecondaryDatabase

  val linkKeySerializer: TupleBinding[UUID]

  val linkSerializer: TupleBinding[Link]

  val linkUrlSerializer: TupleBinding[URLT]

  def push(link: Link, parent: UUID) = {
    val txn = env.beginTransaction(null, null)
    val cursor = urlDatabase.openCursor(txn, null)
    var result: LinkQueue.AddResult = null
    try {
      val uuid = linkKeySerializer.objectToEntry(link.id)
      val url = linkUrlSerializer.objectToEntry(link.link)
      // check if link is in the queue or was processed either as redirect link or as regular one
      cursor.putNoOverwrite(url, uuid) match {
        case OperationStatus.KEYEXIST => // if yes - then update parent link relation and that's it
          val parentUUID = linkKeySerializer.objectToEntry(parent)
          result = relationDatabase.putNoDupData(txn, uuid, parentUUID) match {
            case OperationStatus.SUCCESS =>
              // TODO update link status here
              LinkQueue.Ok
            case _ => LinkQueue.Retry
          }
        case OperationStatus.SUCCESS => // if no - then add link to the database
          val entry = linkSerializer.objectToEntry(link)
          result = mainDatabase.putNoOverwrite(txn, url, entry) match {
            case OperationStatus.SUCCESS => LinkQueue.Ok
            case OperationStatus.KEYEXIST => LinkQueue.Retry
          }
      }
    } finally {
      cursor.close()
      result match {
        case LinkQueue.Ok =>
          txn.commit()
          qSize.incrementAndGet()
        case _ => txn.abort()
      }
    }
    result
  }

  def pop(): Either[PopError, Link] = {
    // take first link which from the queue
    implicit def exception = (e: Throwable) => Left(GenericException(e))
    withCursorInTransaction(queueDatabase) {
      case (cursor, txn) => cursor.getFirst(LockMode.RMW).map {
        case (key, value) =>
          val link = linkSerializer.entryToObject(value)
          // update it's state to "in progress"
          link.storageState match {
            case LinkStorageState.QUEUED =>
              val newLink = link.copy(storageState = LinkStorageState.IN_PROGRESS)
              cursor.putCurrent(linkSerializer.objectToEntry(newLink))
              Right(newLink)
            case LinkStorageState.IN_PROGRESS =>
              Left(AlreadyInProgress)
          }
      }.getOrElse(Left(NoRecordInDatabase))
    }
  }

  def reset() {
    // mark all links "in progress" as queued.
    implicit def exception = (e: Throwable) => Left(e)
    withCursorInTransaction(inprogressDatabase) {
      case (cursor, txn) =>
        cursor.map(LockMode.RMW) {
          case (key, value) =>
            val link = linkSerializer.entryToObject(value)
            linkSerializer.objectToEntry(link.copy(storageState = LinkStorageState.QUEUED), value)
            value
        }
        Right()
    }
  }

  def queueSize(): Long = {
    qSize.get()
  }

  private def withCursorInTransaction[E, A](db: SecondaryDatabase)(block: (SecondaryCursor, Transaction) => Either[E, A])(implicit exception: Throwable => Either[E, A]) = {
    import BDBJEQueue.LOG
    val txn = env.beginTransaction(null, null)
    val cursor = db.openCursor(txn, null)
    var result: Either[E, A] = null
    try {
      result = block(cursor, txn)
    } catch {
      case e: Exception => LOG.error(e, e); exception(e)
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
