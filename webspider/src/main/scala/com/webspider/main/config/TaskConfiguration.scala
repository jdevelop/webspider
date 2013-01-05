package com.webspider.main.config

import com.webspider.storage.Storage
import com.webspider.parser.link.{ApacheCommonsLinkNormalizer, RelativeLinkNormalizer}
import com.webspider.main.filter.FilterTrait
import com.webspider.core.HasLink

case class TaskConfiguration(maxWorkers: Int = 35,
                             maxLinks: Int = 50,
                             storage: Option[Storage] = None,
                             linkNormalizer: RelativeLinkNormalizer = new ApacheCommonsLinkNormalizer,
                             authorityMatcher: FilterTrait[HasLink],
                             showStats: Boolean = true)
