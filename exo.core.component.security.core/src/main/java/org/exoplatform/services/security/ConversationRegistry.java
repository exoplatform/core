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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The eXo Platform SAS .<br/> In-memory registry of user's sessions
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 */
public final class ConversationRegistry
{

   /**
    * "concurrency-level".
    */
   public static final String INIT_PARAM_CONCURRENCY_LEVEL = "concurrency-level";

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.security.core.ConversationRegistry");

   /**
    * Default concurrency level.
    */
   private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

   /**
    * Storage for ConversationStates.
    */
   private final ConcurrentHashMap<StateKey, ConversationState> states;

   /**
    * @see {@link IdentityRegistry}
    */
   private final IdentityRegistry identityRegistry;

   /**
    * @see {@link ListenerService}
    */
   private final ListenerService listenerService;

   /**
    * @param params
    * @param identityRegistry @see {@link IdentityRegistry}
    * @param listenerService @see {@link ListenerService}
    */
   public ConversationRegistry(InitParams params, IdentityRegistry identityRegistry, ListenerService listenerService)
   {
      this(parse(params), identityRegistry, listenerService);
   }

   private static int parse(InitParams ip)
   {
      try
      {
         return Integer.valueOf(ip.getValueParam(INIT_PARAM_CONCURRENCY_LEVEL).getValue());
      }
      catch (NullPointerException e)
      {
         LOG.info("Parameter " + INIT_PARAM_CONCURRENCY_LEVEL + " was not found in configuration, default "
            + DEFAULT_CONCURRENCY_LEVEL + " will be used.");
         return DEFAULT_CONCURRENCY_LEVEL;
      }
      catch (Exception e)
      {
         LOG.error("Can't parse parameter " + INIT_PARAM_CONCURRENCY_LEVEL, e);
         return DEFAULT_CONCURRENCY_LEVEL;
      }
   }

   /**
    * @param concurrencyLevel the estimated number of concurrently updating
    *          threads. The implementation performs internal sizing
    * @param identityRegistry @see {@link IdentityRegistry}
    * @param listenerService @see {@link ListenerService}
    */
   private ConversationRegistry(int concurrencyLevel, IdentityRegistry identityRegistry, ListenerService listenerService)
   {
      this.states = new ConcurrentHashMap<StateKey, ConversationState>(concurrencyLevel, 0.75f, concurrencyLevel);
      this.identityRegistry = identityRegistry;
      this.listenerService = listenerService;
   }

   /**
    * Get ConversationState with specified key.
    * 
    * @param key the key.
    * @return ConversationState.
    */
   public ConversationState getState(StateKey key)
   {
      return states.get(key);
   }

   /**
    * Sets the user's session to the registry and broadcasts ADD_SESSION_EVENT
    * message to interested listeners.
    * 
    * @param key the session identifier.
    * @param session the session.
    * @param makeCurrent the store or not the session into thread local.
    */
   public void register(StateKey key, ConversationState state)
   {
      // supposed that "old" stored value (if any) is no more useful in registry
      // so we "push" it
      // for example - we have to do "login" register with username as a key
      // but it is possible to have more than one state (session) with the same
      // UID so old one will be pushed possible drawback of this case if
      // another "same" login occurs between
      // login and possible use - first state will be just missed
      states.put(key, state);
      try
      {
         listenerService.broadcast("exo.core.security.ConversationRegistry.register", this, state);
      }
      catch (Exception e)
      {
         LOG.error("Broadcast message filed ", e);
      }
   }

   /**
    * Remove ConversationStae with specified key. If there is no more
    * ConversationState for user then remove Identity from IdentityRegistry.
    * 
    * @param key the key.
    * @return removed ConversationState or null.
    */
   public ConversationState unregister(StateKey key)
   {
      return unregister(key, true);
   }

   /**
    * Remove ConversationState with specified key. If there is no more
    * ConversationState for user and <code>unregisterIdentity</code> is true then
    * remove Identity from IdentityRegistry.
    * 
    * @param key the key.
    * @param unregisterIdentity if true and no more ConversationStates for user
    *          then unregister Identity
    * @return removed ConversationState or null.
    */
   public ConversationState unregister(StateKey key, boolean unregisterIdentity)
   {
      ConversationState state = states.remove(key);

      if (state == null)
         return null;

      String userId = state.getIdentity().getUserId();

      List<StateKey> keys = getStateKeys(userId);
      if (unregisterIdentity && keys.size() == 0)
      {
         identityRegistry.unregister(userId);
      }

      try
      {
         listenerService.broadcast("exo.core.security.ConversationRegistry.unregister", this, state);
      }
      catch (Exception e)
      {
         LOG.error("Broadcast message filed ", e);
      }

      return state;
   }

   /**
    * Unregister all conversation states for user with specified Id.
    * 
    * @param userId user Id
    * @return set of unregistered conversation states
    */
   public List<ConversationState> unregisterByUserId(String userId)
   {
      List<ConversationState> states = new ArrayList<ConversationState>();
      for (StateKey key : getStateKeys(userId))
      {
         ConversationState state = unregister(key, false);
         if (state != null)
         {
            states.add(state);
         }
      }
      return states;
   }

   /**
    * @param userId the user identifier.
    * @return list of users ConversationState.
    */
   public List<StateKey> getStateKeys(String userId)
   {
      ArrayList<StateKey> s = new ArrayList<StateKey>();
      for (Map.Entry<StateKey, ConversationState> a : states.entrySet())
      {
         if (a.getValue().getIdentity().getUserId().equals(userId))
            s.add(a.getKey());
      }
      return s;
   }

   /**
    * Remove all ConversationStates.
    */
   void clear()
   {
      states.clear();
   }

}
