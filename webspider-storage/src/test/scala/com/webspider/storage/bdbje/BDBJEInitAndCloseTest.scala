package com.webspider.storage.bdbje

import org.junit.runner.RunWith
import org.scalatest.{MustMatchers, FunSpec}
import org.scalatest.junit.JUnitRunner

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
      import collection.JavaConversions._

      val names: MSet[String] = MSet() ++ List("inProgress", "mainDb", "queueDb", "relationDb", "urlDb")

      env.getDatabaseNames.foreach {
        name =>
          names.contains(name) must be(true)
          names -= name
      }

      names.size must be(0)

      close(identity)
    }
  }

}