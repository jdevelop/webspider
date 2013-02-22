package com.webspider.storage.bdbje

import org.specs2.mutable.{BeforeAfter, Specification}
import com.sleepycat.bind.tuple.TupleBinding
import java.util.UUID
import com.webspider.core.Link
import persist.LinkSerializer
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * User: Eugene Dzhurinsky
 * Date: 2/21/13
 */
@RunWith(classOf[JUnitRunner])
class BDBJEQueueTest extends Specification with BDBJEQueue with BDBJEInitAndClose with TestFolderHelper {

  val linkKeySerializer: TupleBinding[UUID] = LinkSerializer.keySerializer
  val linkUrlSerializer: TupleBinding[Link.URLT] = LinkSerializer.linkUrlSerializer

  lazy val bdbAround = new BeforeAfter {
    def after: Any = {
      close()
    }

    def before: Any = {
      cleanup
      init()
    }
  }

  "BDBJEQueue" should {
    "update records count on database push" in bdbAround {
      val uuid = UUID.randomUUID()
      val parent = UUID.randomUUID()
      val url = "http://123.com"
      val link = Link(url, uuid)
      push(link, parent)
      queueSize() must equalTo(1)
    }
  }

}