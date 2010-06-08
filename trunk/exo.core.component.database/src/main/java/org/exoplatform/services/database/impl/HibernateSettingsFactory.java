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

import org.hibernate.HibernateException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cfg.SettingsFactory;

import java.util.Properties;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: HibernateSettingsFactory.java 5332 2006-04-29 18:32:44Z geaz $
 *          Hibernate's SettingsFactory for configure settings
 * @see SettingsFactory
 */
public class HibernateSettingsFactory extends SettingsFactory
{

   private static final String HIBERNATE_CACHE_PROPERTY = "hibernate.cache.provider_class";

   private ExoCacheProvider cacheProvider;

   public HibernateSettingsFactory(ExoCacheProvider cacheProvider) throws HibernateException
   {
      super();
      this.cacheProvider = cacheProvider;
   }

   protected CacheProvider createCacheProvider(Properties properties)
   {
      properties.setProperty(HIBERNATE_CACHE_PROPERTY, ExoCacheProvider.class.getName());
      return cacheProvider;
   }

}
