package com.webspider.storage.bdbje

import java.io.File
import java.util

import com.sleepycat.bind.tuple.{TupleBinding, TupleInput, TupleOutput}
import com.sleepycat.je._
import com.webspider.storage.MustInitAndClose
import com.webspider.storage.bdbje.BDBJEInitAndClose.QueueKeyBinding

/**
  * User: Eugene Dzhurinsky
  * Date: 2/15/13
  */
object BDBJEInitAndClose {

  private[bdbje] object QueueKeyBinding extends TupleBinding[(Long, String)] {
    override def entryToObject(input: TupleInput): (Long, String) = {
      input.readLong() -> input.readString()
    }

    override def objectToEntry(obj: (Long, String), output: TupleOutput): Unit = {
      output.writeLong(obj._1)
      output.writeString(obj._2)
    }
  }

}

trait BDBJEInitAndClose extends MustInitAndClose[Environment] {

  val dbPath: File

  protected var cfg: EnvironmentConfig = _

  protected var env: Environment = _

  protected var urlDatabase: Database = _

  protected var relationDatabase: Database = _

  protected var queueDatabase: SecondaryDatabase = _

  protected var inprogressDatabase: SecondaryDatabase = _

  override def init() {
    dbPath.mkdirs()
    cfg = new EnvironmentConfig().setAllowCreate(true).setTransactional(true)
    env = new Environment(dbPath, cfg)

    def dbCfg(f: (DatabaseConfig) => DatabaseConfig = identity) = {
      f(new DatabaseConfig().setAllowCreate(true).setTransactional(true))
    }

    urlDatabase = env.openDatabase(null, "urlDb", dbCfg())

    def secondaryCfg(storageClass: StorageClass) = {
      val cfg = new SecondaryConfig().
        setAllowPopulate(true).
        setMultiKeyCreator(new SecondaryMultiKeyCreator {
          def createSecondaryKeys(secondary: SecondaryDatabase,
                                  key: DatabaseEntry,
                                  data: DatabaseEntry,
                                  results: util.Set[DatabaseEntry]) {
            val entry = PartialDataBinding.entryToObject(data)
            if (entry.storageClass == storageClass) {
              val dbe = new DatabaseEntry()
              QueueKeyBinding.objectToEntry(entry.priority -> entry.key, dbe)
              results.add(dbe)
            }
          }
        }
        )
      cfg.setAllowCreate(true)
      cfg.setTransactional(true)
      cfg
    }

    queueDatabase = env.openSecondaryDatabase(null, "queueDb", urlDatabase, secondaryCfg(Queued))
    inprogressDatabase = env.openSecondaryDatabase(null, "inProgress", urlDatabase, secondaryCfg(InProgress))
  }

  override def close(f: Environment => Unit) {
    List(queueDatabase, relationDatabase, inprogressDatabase, urlDatabase).filter(_ != null).foreach {
      closable =>
        try {
          closable.close()
        } catch {
          case e: Exception => e.printStackTrace()
        }
    }
    f(env)
    env.close()
  }

}