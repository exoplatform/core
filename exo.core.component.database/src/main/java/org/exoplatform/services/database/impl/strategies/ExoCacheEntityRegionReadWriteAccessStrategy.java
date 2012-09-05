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
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;

/**
 * @author <a href="dvishinskiy@exoplatform.com">Dmitriy Vishinskiy</a>
 * @version $Id:$
 */
public class ExoCacheEntityRegionReadWriteAccessStrategy extends ExoCacheAccessStrategy implements
   EntityRegionAccessStrategy
{

   public ExoCacheEntityRegionReadWriteAccessStrategy(ExoCacheTransactionalDataRegion region)
   {
      super(region);
   }

   /**
    * @inheritDoc
    */
   public EntityRegion getRegion()
   {
      return (EntityRegion)region;
   }

   /**
    * @inheritDoc
    */
   public boolean insert(Object key, Object value, Object version) throws CacheException
   {
      region.put(key, value);
      return true;
   }

   /**
    * @inheritDoc
    */
   public boolean update(Object key, Object value, Object currentVersion, Object previousVersion) throws CacheException
   {
      region.put(key, value);
      return true;
   }

   /**
    * @inheritDoc
    */
   public boolean afterInsert(Object key, Object value, Object version) throws CacheException
   {
      return false;
   }

   /**
    * @inheritDoc
    */
   public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)
      throws CacheException
   {
      return false;
   }

}
