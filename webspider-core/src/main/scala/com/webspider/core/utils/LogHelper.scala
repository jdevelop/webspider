package com.webspider.core.utils

import org.apache.log4j.Logger

trait LogHelper {

  val loggerName = this.getClass.getName
  lazy val logger = Logger.getLogger(loggerName)

  def debug(msg: => Any) = if (logger.isDebugEnabled) logger.debug(msg)
  def debug(msg: => Any, throwable: => Throwable) = if (logger.isDebugEnabled) logger.debug(msg, throwable)
  def error(msg: => Any) = logger.error(msg)
  def error(msg: => Any, throwable: => Throwable) = logger.error(msg, throwable)
}
