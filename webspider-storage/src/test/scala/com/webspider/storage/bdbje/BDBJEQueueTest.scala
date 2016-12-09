package com.webspider.storage.bdbje

import java.io._
import java.nio.ByteBuffer
import java.util
import java.util.Collections
import java.util.concurrent.{ConcurrentHashMap, CountDownLatch, TimeUnit}
import java.util.function.BiFunction
import java.util.zip.GZIPInputStream

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator
import com.webspider.storage.PersistentQueue.NoRecordInDatabase
import com.webspider.storage.bdbje.BDBJEQueueTest.SampleURL
import com.webspider.storage.{HasStringKey, PersistenceSerializer, PersistentQueue}
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSpec, MustMatchers}

import scala.concurrent.{Await, Future}

/**
  * User: Eugene Dzhurinsky
  * Date: 2/21/13
  */
object BDBJEQueueTest {

  case class SampleURL(url: String, data: String, code: Int) extends HasStringKey {
    override val key = url
  }

  private val KryoSerializer = new ScalaKryoInstantiator().newKryo()

}

@RunWith(classOf[JUnitRunner])
class BDBJEQueueTest
  extends FunSpec
    with MustMatchers
    with BeforeAndAfter {

  trait bdbAround extends BDBJEQueue[SampleURL]
    with TestFolderHelper
    with BDBJEInitAndClose {

    override protected val serializer: PersistenceSerializer[SampleURL] = new PersistenceSerializer[SampleURL] {
      override def serialize(src: SampleURL): ByteBuffer = {
        val os = new ByteArrayOutputStream()
        val dataOut = new DataOutputStream(os)
        dataOut.writeUTF(src.url)
        dataOut.writeUTF(src.data)
        dataOut.writeInt(src.code)
        dataOut.flush()
        dataOut.close()
        os.flush()
        val bb = ByteBuffer.allocate(os.size())
        bb.put(os.toByteArray)
        bb.flip()
        os.close()
        bb
      }

      override def deserialize(src: ByteBuffer): SampleURL = {
        val is = new ByteArrayInputStream(src.array(), src.arrayOffset(), src.capacity())
        val dataInput = new DataInputStream(is)
        SampleURL(
          dataInput.readUTF(),
          dataInput.readUTF(),
          dataInput.readInt()
        )
      }
    }

    import collection.JavaConversions._

    def after: Any = {
      close {
        envmt ⇒ envmt.getDatabaseNames.foreach(envmt.removeDatabase(null, _))
      }
      qSize.set(0)
      FileUtils.deleteDirectory(dbPath)
    }

    def before: Any = {
      dbPath.mkdirs()
      init()
    }

    val url = "http://123.com"
    val link = SampleURL(url, "Test here", 200)

    def testBody: Unit

    def run(): Unit = {
      before
      try {
        testBody
      } finally {
        after
      }
    }

  }


  describe("BDBJEQueue") {

    it("should update records count on database push") {
      new bdbAround {

        override def testBody: Unit = {
          push(link)
          queueSize() must be(1)
        }
      }.run
    }

    it("should populate record from queue") {
      new bdbAround {
        override def testBody {
          push(link) must be(PersistentQueue.Added)
          val newLinkEither = pop()
          newLinkEither.isRight must be(true)
          val newLink = newLinkEither.right.get
          newLink must be(link)
          val shouldNotExist = pop()
          shouldNotExist.isLeft must be(true)
          shouldNotExist.left.get must be(NoRecordInDatabase)
        }
      }.run
    }

    it("should push records concurrently") {
      new bdbAround {
        override def testBody {
          import org.scalacheck.Prop.forAll
          import org.scalacheck._
          val is = new GZIPInputStream(getClass.getResourceAsStream("/urlz.10k.gz"))
          val urlsData = io.Source.fromInputStream(is, "UTF8").getLines().toSeq
          println(urlsData.length)

          implicit val noUrlsShrink = Shrink.shrinkAny[String]
          implicit val noCodesShrink = Shrink.shrinkAny[Int]

          val urlsGen = Gen.oneOf(urlsData)
          val codesGen = Gen.choose(200, 500)

          import scala.concurrent.ExecutionContext.Implicits.global

          val checkSet: ConcurrentHashMap[String, List[Int]] = new ConcurrentHashMap[String, List[Int]]()

          val futures = (1 to 10) map {
            case _ ⇒
              Future {
                forAll(urlsGen, codesGen) {
                  case (urlStr, code) ⇒
                    push(SampleURL(urlStr, "noop", code))
                    checkSet.compute(urlStr, new BiFunction[String, List[Int], List[Int]] {
                      override def apply(t: String, u: List[Int]): List[Int] = {
                        if (u == null) {
                          List(code)
                        } else {
                          code :: u
                        }
                      }
                    })
                    true
                }.check(Test.Parameters.default.withMinSuccessfulTests(1000))
              }
          }

          import concurrent.duration._
          Await.result(Future.sequence(futures), 2 minutes)

          Iterator.continually(pop()).takeWhile(_.isRight) foreach {
            case Right(rec) ⇒
              val cached = checkSet.get(rec.key)
              cached must not be null
              cached must contain(rec.code)
              checkSet.remove(rec.key)
            case _ ⇒ fail("No records")
          }

          checkSet.isEmpty must be(true)

        }
      }.run

    }

  }

}