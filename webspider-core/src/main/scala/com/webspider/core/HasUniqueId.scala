package com.webspider.core

import java.util.UUID

/**
 * Identifies if the object has unique ID.
 */
trait HasUniqueId {

  val id: UUID

}
