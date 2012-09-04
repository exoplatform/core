/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.database.impl;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.impl.CacheServiceImpl;
import org.exoplatform.services.database.impl.regions.ExoCacheCollectionRegion;
import org.exoplatform.services.database.impl.regions.ExoCacheEntityRegion;
import org.exoplatform.services.database.impl.regions.ExoCacheNaturalIdRegion;
import org.exoplatform.services.database.impl.regions.ExoCacheQueryResultsRegion;
import org.exoplatform.services.database.impl.regions.ExoCacheTimestampsRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.Settings;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author <a href="dvishinskiy@exoplatform.com">Dmitriy Vishinskiy</a>
 * @version $Id:$
 */
public class ExoCacheRegionFactory implements RegionFactory
{

   private CacheService cacheService;

   /**
    * Maximum count of items stored in the cache.
    */
   private int MAX_CACHE_SIZE = 5000;

   public ExoCacheRegionFactory()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      this.cacheService = (CacheService)container.getComponentInstanceOfType(CacheServiceImpl.class);
   }

   /**
    * @inheritDoc
    */
   public void start(Settings settings, Properties properties) throws CacheException
   {
      if (properties.containsKey("exocache.config.maxsize"))
      {
         MAX_CACHE_SIZE = Integer.parseInt(properties.get("exocache.config.maxsize").toString());
      }
   }

   /**
    * @inheritDoc
    */
   public void stop()
   {
   }

   /**
    * @inheritDoc
    */
   public boolean isMinimalPutsEnabledByDefault()
   {
      return true;
   }

   /**
    * @inheritDoc
    */
   public AccessType getDefaultAccessType()
   {
      return AccessType.READ_WRITE;
   }

   /**
    * @inheritDoc
    */
   public long nextTimestamp()
   {
      return System.currentTimeMillis() / 100;
   }

   /**
    * @inheritDoc
    */
   public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata)
      throws CacheException
   {
      ExoCache<Serializable, Object> cache = cacheService.getCacheInstance(regionName);
      cache.setMaxSize(MAX_CACHE_SIZE);
      return new ExoCacheEntityRegion(cache, metadata);
   }

   /**
    * @inheritDoc
    */
   public NaturalIdRegion buildNaturalIdRegion(String regionName, Properties properties, CacheDataDescription metadata)
      throws CacheException
   {
      ExoCache<Serializable, Object> cache = cacheService.getCacheInstance(regionName);
      cache.setMaxSize(MAX_CACHE_SIZE);
      return new ExoCacheNaturalIdRegion(cache, metadata);
   }

   /**
    * @inheritDoc
    */
   public CollectionRegion buildCollectionRegion(String regionName, Properties properties, CacheDataDescription metadata)
      throws CacheException
   {
      ExoCache<Serializable, Object> cache = cacheService.getCacheInstance(regionName);
      cache.setMaxSize(MAX_CACHE_SIZE);
      return new ExoCacheCollectionRegion(cache, metadata);
   }

   /**
    * @inheritDoc
    */
   public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException
   {
      ExoCache<Serializable, Object> cache = cacheService.getCacheInstance(regionName);
      cache.setMaxSize(MAX_CACHE_SIZE);
      return new ExoCacheQueryResultsRegion(cache);
   }

   /**
    * @inheritDoc
    */
   public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException
   {
      ExoCache<Serializable, Object> cache = cacheService.getCacheInstance(regionName);
      cache.setMaxSize(MAX_CACHE_SIZE);
      return new ExoCacheTimestampsRegion(cache);
   }
}
