package com.webspider.storage.bdbje

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

/**
  * User: Eugene Dzhurinsky
  * Date: 2/20/13
  */
@RunWith(classOf[JUnitRunner])
class BDBJEInitAndCloseTest extends FunSpec with BDBJEInitAndClose with TestFolderHelper with MustMatchers {

  describe("BDBJEInitAndClose") {
    it("should open database correctly") {
      init()
      val files = dbPath.listFiles()
      files.length must be > 0

      import collection.mutable.{Set => MSet}

      env.getDatabaseNames must contain only("inProgress", "queueDb", "urlDb")

      close(identity)
    }
  }

}