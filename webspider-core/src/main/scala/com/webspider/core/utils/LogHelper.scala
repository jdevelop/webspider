package com.webspider.core.utils

trait LogHelper {
  def debug(msg: Any) = println(msg)
  def error(msg: Any) = println(msg)
}
