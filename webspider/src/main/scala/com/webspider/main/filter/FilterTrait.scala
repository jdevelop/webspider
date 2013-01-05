package com.webspider.main.filter

trait FilterTrait[T] {

  def shallProcess(src: T): Boolean

}
