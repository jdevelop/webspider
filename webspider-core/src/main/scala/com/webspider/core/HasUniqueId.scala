package com.webspider.core

import java.util.UUID

/**
 * Identifies if the object has unique ID.
 */
trait HasUniqueId {

  private var id: UUID = _

  def uniqueId(): UUID = id

  def uniqueId_(id: UUID) {
    this.id = id
  }

}
