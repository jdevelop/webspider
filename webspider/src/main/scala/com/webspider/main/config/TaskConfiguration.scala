package com.webspider.main.config

import com.webspider.main.storage.Storage
import com.webspider.parser.link.{ApacheCommonsLinkNormalizer, RelativeLinkNormalizer}
import com.webspider.main.filter.AuthorityMatcher

case class TaskConfiguration(maxWorkers: Int = 35,
                             maxLinks: Int = 50,
                             storage: Option[Storage] = None,
                             linkNormalizer: RelativeLinkNormalizer = new ApacheCommonsLinkNormalizer,
                             authorityMatcher: AuthorityMatcher,
                             showStats: Boolean = true)
