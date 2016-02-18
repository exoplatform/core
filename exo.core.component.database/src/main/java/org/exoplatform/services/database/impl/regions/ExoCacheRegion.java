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
package org.exoplatform.services.database.impl.regions;

import org.exoplatform.services.cache.ExoCache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.Region;

import java.io.Serializable;
import java.util.Map;

/**
 * @author <a href="dvishinskiy@exoplatform.com">Dmitriy Vishinskiy</a>
 * @version $Id:$
 */
public class ExoCacheRegion implements Region
{

   ExoCache<Serializable, Object> cache;

   public ExoCacheRegion(ExoCache<Serializable, Object> cache)
   {
      this.cache = cache;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return cache.getName();
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
   public boolean contains(Object key)
   {
      return cache.get((Serializable)key) != null;
   }

   /**
    * {@inheritDoc}
    */
   public long getSizeInMemory()
   {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public long getElementCountInMemory()
   {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public long getElementCountOnDisk()
   {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public Map<Serializable, Object> toMap()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public long nextTimestamp()
   {
      return System.currentTimeMillis() / 100;
   }

   /**
    * {@inheritDoc}
    */
   public int getTimeout()
   {
      return 60000;
   }

}
