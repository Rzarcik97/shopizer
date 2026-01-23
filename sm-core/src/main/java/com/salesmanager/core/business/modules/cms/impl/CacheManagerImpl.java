package com.salesmanager.core.business.modules.cms.impl;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CacheManagerImpl implements CacheManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagerImpl.class);

  //private static final String LOCATION_PROPERTIES = "location";

  protected String location = null;

  @SuppressWarnings("rawtypes")
  private Cache<String, Object> cache;

  @SuppressWarnings("unchecked")
  protected void init(String namedCache, String locationFolder) {

    try {

      this.location = locationFolder;
      // manager = new DefaultCacheManager(repositoryFileName);

      VendorCacheManager manager = VendorCacheManager.getInstance();

      if (manager == null) {
        LOGGER.error("CacheManager is null");
        return;
      }

/*      @SuppressWarnings("rawtypes")
      Cache c = manager.getManager().getCache(namedCache);

      if(c != null) {
    	  f = new TreeCacheFactory();
    	  treeCache = f.createTreeCache(c);
    	  //this.treeCache = (TreeCache)c;
    	  return;
      }*/


      Configuration config = new ConfigurationBuilder()
              .persistence()
              .passivation(false)
              .addSoftIndexFileStore()
              .dataLocation(location + "/data")     // Gdzie przechowywać dane
              .indexLocation(location + "/index")   // Gdzie przechowywać indeks
              .segmented(true)
              .preload(false)
              .shared(false)
              .async()
              .enable()
              .build();

      manager.getManager().defineConfiguration(namedCache, config);

      this.cache = manager.getManager().getCache(namedCache);

      LOGGER.debug("CMS started");



    } catch (Exception e) {
      LOGGER.error("Error while instantiating CmsImageFileManager", e);
    } finally {
    }
  }

  public EmbeddedCacheManager getManager() {
    return VendorCacheManager.getInstance().getManager();
  }

  @SuppressWarnings("rawtypes")
  public Cache<String, Object> getCache() {
    return cache;
  }

  // HELPER METHODS (zostaw je na razie, będą przydatne przy refaktoryzacji)

  public String createKey(String path, String key) {
    if (path == null || path.isEmpty()) {
      return key;
    }
    String normalizedPath = path.replaceAll("/+", "/");
    if (normalizedPath.endsWith("/")) {
      return normalizedPath + key;
    }
    return normalizedPath + "/" + key;
  }

  public Object get(String path, String key) {
    return cache != null ? cache.get(createKey(path, key)) : null;
  }

  public void put(String path, String key, Object value) {
    if (cache != null) {
      cache.put(createKey(path, key), value);
    }
  }

  public Object remove(String path, String key) {
    return cache != null ? cache.remove(createKey(path, key)) : null;
  }
}
