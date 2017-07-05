package com.webspider.parser

import java.io.{File, FileInputStream}

import com.webspider.core.Resource
import com.webspider.parser
import com.webspider.parser.link.ApacheCommonsLinkNormalizer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

import scala.collection.mutable.{Map ⇒ MMap}

@RunWith(classOf[JUnitRunner])
class HtmlParserTest extends FunSpec with MustMatchers {

  val resources = if (new File("./webspider-parser").exists()) new File("./webspider-parser/src/test/resources/htmlparser/") else new File("src/test/resources/htmlparser/")

  val docsDir = new File(resources, "document")
  val linksDir = new File(resources, "links")
  val linkDir = new File(resources, "link")

  describe("Parser") {
    it("should correctly find all links in given documents") {
      val pairs = for (
        pair@(link, doc, links) <- docsDir.listFiles().map {
          f: File => (new File(linkDir, f.getName), f, new File(linksDir, f.getName))
        }
        if doc.exists() && links.exists() && link.exists()
      ) yield pair
      pairs.foreach {
        case (link, doc, links) =>
          val url = Resource(io.Source.fromFile(link).getLines().next())
          val linkMap: MMap[String, Int] = io.Source.fromFile(links).getLines().foldLeft(MMap[String, Int]()) {
            case (map, linkString) => map += (linkString -> 0)
          }
          HtmlParser(url.location, parser.extractorDefaults(src ⇒ ApacheCommonsLinkNormalizer.normalize(url.location, src)))
            .parse(new FileInputStream(doc))
            .foreach {
              tr ⇒
                for (
                  count <- linkMap.get(tr.src)
                ) {
                  linkMap.update(tr.src, count + 1)
                }
            }
          val hasZeroLinks = linkMap.filter(_._2 == 0)
          assert(
            hasZeroLinks.isEmpty, hasZeroLinks.keys.mkString(",")
          )
      }
    }
  }

}
