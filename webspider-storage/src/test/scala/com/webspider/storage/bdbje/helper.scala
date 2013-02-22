package com.webspider.storage.bdbje

import java.io.File

/**
 * User: Eugene Dzhurinsky
 * Date: 2/21/13
 */
trait TestFolderHelper {

  val dbPath = new File(new File(System.getProperty("java.io.tmpdir")), "bdbFolder")

  def cleanup = if (!dbPath.mkdirs()) {
    def removeFiles(f: File) {
      val (dirs, files) = f.listFiles().partition(_.isDirectory)
      files.foreach(_.delete())
      dirs.foreach {
        dir => removeFiles(dir);
        dir.delete()
      }
    }
    removeFiles(dbPath)
    dbPath.mkdirs()
  }

}
