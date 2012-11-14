package com.webspider.main.config

import com.webspider.main.storage.Storage

class TaskConfiguration {
  var maxWorkers = 15
  var maxLinks = 10
  var storage: Option[Storage] = None
}
