package com.salesmanager.core.business.modules.cms.impl;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

public interface CacheManager extends CMSManager {

  EmbeddedCacheManager getManager();

  @SuppressWarnings("rawtypes")
  Cache<String, Object> getCache();

}
