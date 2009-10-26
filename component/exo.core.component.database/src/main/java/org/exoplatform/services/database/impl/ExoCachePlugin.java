/*
 * Copyright (C) 2009 eXo Platform SAS.
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

import org.exoplatform.services.cache.ExoCache;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

import java.io.Serializable;
import java.util.Map;

/**
 * Jul 17, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ExoCachePlugin.java,v 1.1 2004/08/29 21:47:58 benjmestrallet
 *           Exp $
 */
public class ExoCachePlugin implements Cache
{

   private ExoCache<Serializable, Object> cache_;

   public ExoCachePlugin(ExoCache<Serializable, Object> cache)
   {
      cache_ = cache;
   }

   /**
    * {@inheritDoc}
    */
   public Object get(Object key) throws CacheException
   {
      try
      {
         return cache_.get((Serializable)key);
      }
      catch (Exception ex)
      {
         throw new CacheException(ex);
      }
   }

   /**
    * Get an item from the cache
    * 
    * @param key
    * @return the cached object or <tt>null</tt>
    * @throws CacheException
    */
   public Object read(Object key) throws CacheException
   {
      return get(key);
   }

   /**
    * {@inheritDoc}
    */
   public void put(Object key, Object value) throws CacheException
   {
      try
      {
         cache_.put((Serializable)key, (Serializable)value);
      }
      catch (Exception ex)
      {
         throw new CacheException(ex);
      }
   }

   /**
    * Add an item to the cache
    * 
    * @param key
    * @param value
    * @throws CacheException
    */
   public void update(Object key, Object value) throws CacheException
   {
      put(key, value);
   }

   /**
    * {@inheritDoc}
    */
   public void remove(Object key) throws CacheException
   {
      try
      {
         cache_.remove((Serializable)key);
      }
      catch (Exception ex)
      {
         throw new CacheException(ex);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void clear() throws CacheException
   {
      try
      {
         cache_.clearCache();
      }
      catch (Exception ex)
      {
         throw new CacheException(ex);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void destroy() throws CacheException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void lock(Object key) throws CacheException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void unlock(Object key) throws CacheException
   {
   }

   /**
    * {@inheritDoc}
    */
   public long nextTimestamp()
   {
      return Timestamper.next();
   }

   /**
    * Returns the lock timeout for this cache.
    */
   public int getTimeout()
   {
      return Timestamper.ONE_MS * 60000;
   }

   /**
    * Get the name of the cache region
    */
   public String getRegionName()
   {
      return cache_.getName();
   }

   /**
    * The number of bytes is this cache region currently consuming in memory.
    * 
    * @return The number of bytes consumed by this region; -1 if unknown or
    *         unsupported.
    */
   public long getSizeInMemory()
   {
      return -1;
   }

   /**
    * The count of entries currently contained in the regions in-memory store.
    * 
    * @return The count of entries in memory; -1 if unknown or unsupported.
    */
   public long getElementCountInMemory()
   {
      return -1;
   }

   /**
    * The count of entries currently contained in the regions disk store.
    * 
    * @return The count of entries on disk; -1 if unknown or unsupported.
    */
   public long getElementCountOnDisk()
   {
      return -1;
   }

   /**
    * optional operation
    */
   @SuppressWarnings("unchecked")
   public Map toMap()
   {
      return null;
   }

}
