package com.webspider.storage.bdbje

import org.specs2.mutable.Specification
import java.io.File
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * User: Eugene Dzhurinsky
 * Date: 2/20/13
 */
@RunWith(classOf[JUnitRunner])
class BDBJEInitAndCloseTest extends Specification with BDBJEInitAndClose {

  val folder = new File(new File(System.getProperty("java.io.tmpdir")), "bdbFolder")
  folder.mkdirs() must be equalTo true

  val dbPath = folder

  "BDBJEInitAndClose" should {
    "open database correctly" in {
      init()
      val files = folder.listFiles()
      files.length must be greaterThan 0

      import collection.mutable.{Set => MSet}
      import collection.JavaConversions._

      val names: MSet[String] = MSet() ++ List("inProgress", "mainDb", "queueDb", "relationDb", "urlDb")

      env.getDatabaseNames.foreach {
        name =>
          names.contains(name) must be equalTo true
          names -= name
      }

      names.size must be equalTo 0

      files.foreach {
        file => file.exists() must be equalTo true
        file.delete()
      }
      folder.delete() must be equalTo true
    }
  }

}