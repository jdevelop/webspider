package com.spidersaas.parser

import java.io.InputStream
import com.spidersaas.core.Link
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.Attributes

class HtmlParser(current: Link, linkListener: LinkListener[Link]) extends DocumentParser[InputStream, Link] {

  class SaxParser extends DefaultHandler {

    override def startElement(uri: String, localName: String, qName: String, attributes: Attributes) {}

    override def endElement(uri: String, localName: String, qName: String) {}

    override def characters(ch: Array[Char], start: Int, length: Int) {}

  }

  override def parse(source: InputStream) {
    null
  }

}
