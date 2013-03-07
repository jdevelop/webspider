package com.webspider.storage.bdbje

import java.io.File

/**
 * User: Eugene Dzhurinsky
 * Date: 2/21/13
 */
trait TestFolderHelper extends BDBJEInitAndClose {

  val dbPath = new File(new File(System.getProperty("java.io.tmpdir")), "bdbFolder")

}
