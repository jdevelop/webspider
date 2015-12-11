package com.webspider.storage.bdbje

import com.sleepycat.bind.tuple.TupleBinding
import java.util.UUID
import com.webspider.core.Link
import org.scalatest.{BeforeAndAfter, MustMatchers, FunSpec}
import org.scalatest.junit.JUnitRunner
import persist.LinkSerializer
import org.junit.runner.RunWith
import com.webspider.storage.LinkQueue
import com.webspider.storage.LinkQueue.NoRecordInDatabase

/**
  * User: Eugene Dzhurinsky
  * Date: 2/21/13
  */
@RunWith(classOf[JUnitRunner])
class BDBJEQueueTest extends FunSpec with BDBJEQueue with BDBJEInitAndClose with TestFolderHelper with MustMatchers with BeforeAndAfter {

  val linkKeySerializer: TupleBinding[UUID] = LinkSerializer.keySerializer
  val linkUrlSerializer: TupleBinding[Link.URLT] = LinkSerializer.linkUrlSerializer

  trait bdbAround {

    import collection.JavaConversions._

    def after: Any = {
      close {
        envmt ⇒ envmt.getDatabaseNames.foreach(envmt.removeDatabase(null, _))
      }
      qSize.set(0)
    }

    def before: Any = {
      dbPath.mkdirs()
      init()
    }

    val uuid = UUID.randomUUID()
    val parent = UUID.randomUUID()
    val url = "http://123.com"
    val link = Link(url, uuid)

    def testBody: Unit

    def run(): Unit = {
      before
      testBody
      after
    }

  }


  describe("BDBJEQueue") {

    it("should update records count on database push") {
      new bdbAround {
        override def testBody: Unit = {
          push(link, parent)
          queueSize() must be(1)
        }
      }
    }

    it("should update record parent and keep single link") {
      new bdbAround {
        override def testBody {
          val linkCopy = Link("http://123.com", UUID.randomUUID())
          push(link, UUID.randomUUID()) must be(LinkQueue.Ok)
          push(linkCopy, UUID.randomUUID()) must be(LinkQueue.Ok)
          urlDatabase.count() must be(1)
          queueSize() must be(1)
          mainDatabase.count() must be(1)
          relationDatabase.count() must be(2)
        }
      }
    }

    it("should populate record from queue and wait for another one") {
      new bdbAround {
        override def testBody {
          push(link, UUID.randomUUID()) must be(LinkQueue.Ok)
          val newLinkEither = pop()
          newLinkEither.isRight must be(true)
          val newLink: Link = newLinkEither.right.get
          newLink.id must be(link.id)
          val shouldNotExist = pop()
          shouldNotExist.isLeft must be(true)
          shouldNotExist.left.get must be(NoRecordInDatabase)
        }
      }
    }

  }

}