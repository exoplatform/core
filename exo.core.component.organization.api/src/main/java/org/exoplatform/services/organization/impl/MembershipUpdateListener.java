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
package org.exoplatform.services.organization.impl;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.services.security.StateKey;

import java.util.Iterator;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class MembershipUpdateListener extends MembershipEventListener
{

   /** Logger. */
   private static final Log LOG = ExoLogger.getLogger(MembershipUpdateListener.class.getName());

   /** @see ConversationRegistry. */
   private ConversationRegistry conversationRegistry;

   public MembershipUpdateListener(ConversationRegistry conversationRegistry)
   {
      this.conversationRegistry = conversationRegistry;
   }

   // >>>>>>>
   // Update Identity in each ConversationState. In fact each user may have few
   // ConversationStates but IdentityRegistry keeps Identity that was created
   // when user log-in last time. Any way Identity in IdentityRegistry will be
   // updated through ConversationRegistry.
   // If multi-login is disabled (see DefaultLoginModule, option
   // 'singleLogin'). Then updating may be more simple, in this case enough
   // just remove ConversationState (it should be only one) for specified user,
   // then update Identity in IdentityRegistry. ConversationRegistry will be
   // updated by SetCurrentIdentityFilter in next request.

   /**
    * {@inheritDoc}
    */
   @Override
   public void postDelete(Membership m) throws Exception
   {
      if (LOG.isDebugEnabled())
         LOG.debug(">>> In postDelete");
      String userId = m.getUserName();
      MembershipEntry expected = new MembershipEntry(m.getGroupId(), m.getMembershipType());
      for (StateKey key : conversationRegistry.getStateKeys(userId))
      {
         ConversationState cstate = conversationRegistry.getState(key);
         Identity identity = cstate.getIdentity();
         Iterator<MembershipEntry> iter = identity.getMemberships().iterator();
         while (iter.hasNext())
         {
            MembershipEntry tmp = iter.next();
            if (tmp.equals(expected))
            {
               iter.remove();
               if (LOG.isDebugEnabled())
                  LOG.debug("Removed membership entry " + tmp);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void postSave(Membership m, boolean isNew) throws Exception
   {
      if (LOG.isDebugEnabled())
         LOG.debug(">>> In postSave");
      String userId = m.getUserName();
      MembershipEntry me = new MembershipEntry(m.getGroupId(), m.getMembershipType());
      for (StateKey key : conversationRegistry.getStateKeys(userId))
      {
         ConversationState cstate = conversationRegistry.getState(key);
         Identity identity = cstate.getIdentity();
         Iterator<MembershipEntry> iter = identity.getMemberships().iterator();
         boolean contains = false;
         while (iter.hasNext())
         {
            if (iter.next().equals(me))
            {
               contains = true;
               break;
            }
         }
         if (!contains)
         {
            identity.getMemberships().add(me);
            if (LOG.isDebugEnabled())
               LOG.debug("Added membership entry " + me);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void preDelete(Membership m) throws Exception
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void preSave(Membership m, boolean isNew) throws Exception
   {
   }

}
