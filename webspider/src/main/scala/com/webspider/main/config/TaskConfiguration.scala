package com.webspider.main.config

import com.webspider.main.storage.Storage

class TaskConfiguration {
  var maxWorkers = 35
  var maxLinks = 50
  var storage: Option[Storage] = None
  var showStats = true
}
