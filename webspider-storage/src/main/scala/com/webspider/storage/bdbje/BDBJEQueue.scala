package com.webspider.storage.bdbje

import com.webspider.storage.{MustInitAndClose, LinkQueue}
import com.webspider.core.Link
import com.sleepycat.bind.tuple.TupleBinding
import java.util.UUID
import com.sleepycat.je._

import Link.URLT

trait BDBJEQueue extends LinkQueue {

  self: MustInitAndClose =>

  val env: Environment

  // contains UUID -> Record mappings
  val mainDatabase: Database

  // Containts redirect URL -> UUID mapping
  val urlDatabase: Database

  // Contains mappings UUID -> parent UUID
  val relationDatabase: Database

  val queueDatabase: SecondaryDatabase

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
        case LinkQueue.Ok => txn.commit()
        case _ => txn.abort()
      }
    }
    result
  }

  def pop(): Option[Link] = {
    // take first link which from the queue
    val txn = env.beginTransaction(null, null);
    val cursor = queueDatabase.openCursor(txn, null)
    cursor.getFirst(LockMode.READ_COMMITTED).map {
      case (key, value) =>
        val link = linkSerializer.entryToObject(value)
        // update it's state to "in progress"
        link
    }
  }

  def reset() {
    // mark all links "in progress" as queued.
  }

  def queueSize(): Int = {
    // return size of queue, e.g links being processed.
    0
  }

}
