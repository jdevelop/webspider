package com.webspider.config

import java.io.{File, FileFilter}

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger

/**
  * Configuration entry point
  */
object Config {

  val LOG = Logger(Config.getClass)

  private[this] def configDir =
    sys.props.get("WEBSPIDER_CONFIG_HOME").map {
      fname ⇒ new File(fname)
    } getOrElse {
      val home = sys.env("HOME")
      new File(new File(home), ".webspider")
    }

  lazy val cfg: Config = {
    val c = {
      val dir = configDir
      LOG.trace("Reading files from {}", configDir)
      if (dir.exists()) {
        dir.listFiles(new FileFilter {
          override def accept(pathname: File): Boolean =
            pathname.getName.endsWith(".conf") || pathname.getName.endsWith(".properties")
        }).foldLeft(ConfigFactory.empty()) {
          case (lc, src) ⇒
            LOG.trace("Loading configuration from '{}'", src)
            lc.withFallback(ConfigFactory.parseFile(src))
        }
      } else {
        ConfigFactory.empty()
      }
    }.withFallback(ConfigFactory.load("webspider_config"))
      .withFallback(ConfigFactory.load("webspider_config_reference"))
      .withFallback(ConfigFactory.load())
    LOG.trace(s"Config rendered as ${c.root().render()}")
    c
  }

}
