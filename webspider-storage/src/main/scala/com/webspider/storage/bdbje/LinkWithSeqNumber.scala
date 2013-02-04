package com.webspider.storage.bdbje

import com.webspider.core.Link

trait LinkWithSeqNumber {

  self: Link =>

  val number: Long

}
