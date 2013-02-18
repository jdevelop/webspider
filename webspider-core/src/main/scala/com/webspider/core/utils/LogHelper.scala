package com.webspider.core.utils

import org.apache.log4j.Logger

trait LogHelper {

  lazy val logger = Logger.getLogger(this.getClass.getName)

  def info(msg: => Any) {
    if (logger.isInfoEnabled) logger.info(msg)
  }

  def info(msg: => Any, throwable: => Throwable) {
    if (logger.isInfoEnabled) logger.info(msg, throwable)
  }

  def debug(msg: => Any) {
    if (logger.isDebugEnabled) logger.debug(msg)
  }

  def debug(msg: => Any, throwable: => Throwable) {
    if (logger.isDebugEnabled) logger.debug(msg, throwable)
  }

  def error(msg: => Any) {
    logger.error(msg)
  }

  def error(msg: => Any, throwable: => Throwable) {
    logger.error(msg, throwable)
  }

  def logSeparator = info("=" * 50)
}
