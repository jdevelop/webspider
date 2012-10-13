package com.webspider.parser

import link.SimpleLinkNormalizer
import org.scalatest.FlatSpec
import java.io.{FileInputStream, File}
import com.webspider.core.Link

import collection.mutable.{Map => MMap}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HtmlParserTest extends FlatSpec {

  val resources = new File("src/test/resources/htmlparser/")
  val docsDir = new File(resources, "document")
  val linksDir = new File(resources, "links")
  val linkDir = new File(resources, "link")

  println(resources.getAbsolutePath)

  "Parser" should "correctly find all links in given documents" in {
    val pairs = for (
      pair@(link, doc, links) <- docsDir.listFiles().map {
        case f: File => (new File(linkDir, f.getName), f, new File(linksDir, f.getName))
      }
      if (doc.exists() && links.exists() && link.exists())
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
          val linkNormalizer = new SimpleLinkNormalizer
        }.parse(new FileInputStream(doc))
        val hasZeroLinks: Iterable[Boolean] = linkMap.values.map(_ > 0)
        assert(
          hasZeroLinks.forall {
            case z => z
          }
        )
    }
  }

}
