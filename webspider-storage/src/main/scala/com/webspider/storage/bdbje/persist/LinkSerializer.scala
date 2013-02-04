package com.webspider.storage.bdbje.persist

import com.sleepycat.bind.tuple.{TupleInput, TupleOutput, TupleBinding}
import com.webspider.core.Link

class LinkSerializer extends TupleBinding[Link] {

  def entryToObject(input: TupleInput): Link = {
    null
  }

  def objectToEntry(p1: Link, p2: TupleOutput) {

  }

}
