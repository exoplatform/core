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
package org.exoplatform.services.security;

import org.exoplatform.container.spi.DefinitionByType;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
@DefinitionByType
public class IdentityRegistry
{

   /**
    * "concurrency-level".
    */
   public static final String INIT_PARAM_CONCURRENCY_LEVEL = "concurrency-level";

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.security.core.IdentityRegistry");

   /**
    * Default concurrency level.
    */
   private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

   /**
    * Identities.
    */
   private final ConcurrentHashMap<String, Identity> identities;

   public IdentityRegistry(InitParams params)
   {
      this(parseConcurrencyLevel(params));
   }

   /**
    * Try to parse concurrency level attribute from init parameters.
    * 
    * @param params See {@link InitParams}
    * @return parsed parameter or default if parameter not set or can't be parsed
    */
   private static int parseConcurrencyLevel(InitParams params)
   {
      try
      {
         if (params != null)
         {
            ValueParam concurrencyLevel = params.getValueParam(INIT_PARAM_CONCURRENCY_LEVEL);

            if (concurrencyLevel != null)
            {
               return Integer.valueOf(concurrencyLevel.getValue());
            }
         }

         return DEFAULT_CONCURRENCY_LEVEL;
      }
      catch (NumberFormatException e)
      {
         LOG.error("Can't parse parameter " + INIT_PARAM_CONCURRENCY_LEVEL, e);
         return DEFAULT_CONCURRENCY_LEVEL;
      }
   }

   /**
    * Create identity registry.
    * @param concurrencyLevel concurrency level for ConcurrentHashMap 
    */
   private IdentityRegistry(int concurrencyLevel)
   {
      identities = new ConcurrentHashMap<String, Identity>(concurrencyLevel, 0.75f, concurrencyLevel);
   }

   /**
    * Get identity for supplied user ID.
    * @param userId user ID
    * @return identity or null if not found
    */
   public Identity getIdentity(String userId)
   {
      return identities.get(userId);
   }

   /**
    * Register new identity in registry.
    * @param identity {@link Identity}
    */
   public void register(Identity identity)
   {
      this.identities.put(identity.getUserId(), identity);
   }

   /**
    * Remove identity with supplied user ID. 
    * @param userId user ID
    */
   public void unregister(String userId)
   {
      this.identities.remove(userId);
   }

   /**
    * Remove all identities.
    */
   void clear()
   {
      identities.clear();
   }

}
