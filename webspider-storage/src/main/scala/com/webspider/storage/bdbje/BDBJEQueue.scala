package com.webspider.storage.bdbje

import com.webspider.storage.{MustInitAndClose, LinkQueue}
import com.webspider.core.{LinkStorageState, Link}
import com.sleepycat.bind.tuple.TupleBinding
import java.util.UUID
import com.sleepycat.je._

import Link.URLT
import com.webspider.storage.LinkQueue.{GenericException, NoRecordInDatabase, AlreadyInProgress, PopError}
import java.util.concurrent.atomic.AtomicLong
import grizzled.slf4j.Logger

object BDBJEQueue {

  val LOG = Logger(classOf[BDBJEQueue])

}

import BDBJEQueue.LOG

trait BDBJEQueue extends LinkQueue with MustInitAndClose[Environment] {

  val qSize = new AtomicLong(0)

  protected def env: Environment

  // contains UUID -> Record mappings
  protected def mainDatabase: Database

  // Containts redirect URL -> UUID mapping
  protected def urlDatabase: Database

  // Contains mappings UUID -> parent UUID
  protected def relationDatabase: Database

  // holds queue of links
  protected def queueDatabase: SecondaryDatabase

  // holds current "in-progress" links
  protected def inprogressDatabase: SecondaryDatabase

  protected val linkKeySerializer: TupleBinding[UUID]

  val linkSerializer: TupleBinding[Link]

  val linkUrlSerializer: TupleBinding[URLT]

  private[this] def dump(e: DatabaseEntry) = {
    e.getSize.toString + "=>" + e.getData.map("%02X" format _).mkString
  }

  def push(link: Link, parent: UUID) = {
    val txn = env.beginTransaction(null, null)
    val cursor = urlDatabase.openCursor(txn, null)
    var result: LinkQueue.AddResult = null
    try {
      val parentUUID = linkKeySerializer.objectToEntry(parent)
      val uuid = linkKeySerializer.objectToEntry(link.id)
      val url = linkUrlSerializer.objectToEntry(link.link)
      // check if link is in the queue or was processed either as redirect link or as regular one
      LOG.debug("Pushing record '" + link.link + "'")

      def putRelationData = relationDatabase.putNoDupData(txn, uuid, parentUUID) match {
        case OperationStatus.SUCCESS =>
          // TODO update link status here
          LOG.debug("Relation added " + link.id + " => " + parent)
          LinkQueue.Ok
        case _ =>
          LOG.warn("Relation exists " + link.id + " => " + parent + ", should retry")
          LinkQueue.Retry
      }

      cursor.putNoOverwrite(url, uuid) match {
        case OperationStatus.KEYEXIST => // if yes - then update parent link relation and that's it
          LOG.debug("Link exists '" + link.link + "'")
          LOG.debug("Update relation " + link.id + " => " + parent)
          result = putRelationData
        case OperationStatus.SUCCESS => // if no - then add link to the database
          val entry = linkSerializer.objectToEntry(link)
          LOG.debug("Adding new link to queue: '" + link.link + "'")
          result = mainDatabase.putNoOverwrite(txn, url, entry) match {
            case OperationStatus.SUCCESS =>
              LOG.debug("Link added to queue: " + link.id + " => " + link.link)
              LOG.debug("Key: " + dump(url))
              qSize.incrementAndGet()
              putRelationData
            case OperationStatus.KEYEXIST =>
              LOG.warn("Link exists in the queue: " + link.id + " => " + link.link + ", retrying")
              LinkQueue.Retry
          }
      }
    } finally {
      cursor.close()
      result match {
        case LinkQueue.Ok =>
          txn.commit()
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
        case (key, pKey, value) =>
          val link = linkSerializer.entryToObject(value)
          LOG.debug("KEY: " + dump(pKey))
          // update it's state to "in progress"
          link.storageState match {
            case LinkStorageState.QUEUED =>
              val newLink = link.copy(storageState = LinkStorageState.IN_PROGRESS)
              mainDatabase.put(txn, pKey, linkSerializer.objectToEntry(newLink))
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
      case (cursor, txn) => cursor.map(LockMode.RMW) {
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
