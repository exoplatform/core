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
package org.exoplatform.services.database.impl.strategies;

import org.exoplatform.services.database.impl.regions.ExoCacheTransactionalDataRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.RegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;

/**
 * @author <a href="dvishinskiy@exoplatform.com">Dmitriy Vishinskiy</a>
 * @version $Id:$
 */
public class ExoCacheAccessStrategy implements RegionAccessStrategy
{

   ExoCacheTransactionalDataRegion region;

   public ExoCacheAccessStrategy(ExoCacheTransactionalDataRegion region)
   {
      this.region = region;
   }

   /**
    * {@inheritDoc}
    */
   public Object get(Object key, long txTimestamp) throws CacheException
   {
      return region.get(key);
   }

   /**
    * {@inheritDoc}
    */
   public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version) throws CacheException
   {
      region.put(key, value);
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)
      throws CacheException
   {
      region.put(key, value);
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public SoftLock lockItem(Object key, Object version) throws CacheException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public SoftLock lockRegion() throws CacheException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void unlockItem(Object key, SoftLock lock) throws CacheException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void unlockRegion(SoftLock lock) throws CacheException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void remove(Object key) throws CacheException
   {
      evict(key);
   }

   /**
    * {@inheritDoc}
    */
   public void removeAll() throws CacheException
   {
      evictAll();
   }

   /**
    * {@inheritDoc}
    */
   public void evict(Object key) throws CacheException
   {
      region.evict(key);
   }

   /**
    * {@inheritDoc}
    */
   public void evictAll() throws CacheException
   {
      region.evictAll();
   }

}
