package com.webspider.parser

import java.io.{File, FileInputStream}

import com.webspider.core.Link
import com.webspider.parser.link.ApacheCommonsLinkNormalizer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

import scala.collection.mutable.{Map => MMap}

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
          case f: File => (new File(linkDir, f.getName), f, new File(linksDir, f.getName))
        }
        if doc.exists() && links.exists() && link.exists()
      ) yield pair
      pairs.foreach {
        case (link, doc, links) =>
          val url = new Link(io.Source.fromFile(link).getLines().next())
          val linkMap: MMap[String, Int] = io.Source.fromFile(links).getLines().foldLeft(MMap[String, Int]()) {
            case (map, linkString) => map += (linkString -> 0)
          }
          val listener = new LinkListener[Link] {
            def linkFound(parent: Link, link: Link) {
              for (
                count <- linkMap.get(link.link)
              ) {
                linkMap.update(link.link, count + 1)
              }
            }
          }
          new HtmlParser(url, listener) {
            val linkNormalizer = new ApacheCommonsLinkNormalizer
          }.parse(new FileInputStream(doc))
          val hasZeroLinks = linkMap.filter {
            case (k, v) => v == 0
          }
          assert(
            hasZeroLinks.size == 0, hasZeroLinks.keys.mkString(",")
          )
      }
    }
  }

}
