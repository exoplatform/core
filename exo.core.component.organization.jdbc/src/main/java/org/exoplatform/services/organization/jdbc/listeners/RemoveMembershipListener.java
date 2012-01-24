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
package org.exoplatform.services.organization.jdbc.listeners;

import org.exoplatform.services.database.DBObjectQuery;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.jdbc.MembershipDAOImpl;
import org.exoplatform.services.organization.jdbc.MembershipImpl;

import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Le Bien Thuy thuy.le@exoplatform.com
 * Jun 28, 2007
 */
public class RemoveMembershipListener extends Listener<Object, Object>
{
   private OrganizationService service_;

   protected static final Log LOG = ExoLogger
      .getLogger("exo.core.component.organization.jdbc.RemoveMembershipListener");

   public RemoveMembershipListener(OrganizationService service)
   {
      service_ = service;
   }

   @SuppressWarnings("unchecked")
   public void onEvent(Event<Object, Object> event) throws Exception
   {

      Object target = event.getData();
      MembershipHandler membershipHanler = service_.getMembershipHandler();
      if (target instanceof User)
      {
         User user = (User)target;
         LOG.info("Remove all Membership by User: " + user.getUserName());
         membershipHanler.removeMembershipByUser(user.getUserName(), true);
      }
      else if (target instanceof Group)
      {
         Group group = (Group)target;
         LOG.info("Remove all Membership by Group: " + group.getGroupName());
         List<Membership> members = (List<Membership>)membershipHanler.findMembershipsByGroup(group);
         for (Membership member : members)
         {
            membershipHanler.removeMembership(member.getId(), true);
         }
      }
      else if (target instanceof MembershipType)
      {
         try
         {
            MembershipType memberType = (MembershipType)target;
            MembershipDAOImpl mtHandler = (MembershipDAOImpl)service_.getMembershipHandler();
            DBObjectQuery<MembershipImpl> query = new DBObjectQuery<MembershipImpl>(MembershipImpl.class);
            query.addEQ("MEMBERSHIP_TYPE", memberType.getName());
            mtHandler.removeMemberships(query, true);
         }
         catch (Exception e)
         {
            LOG.error("Error while removing a Membership", e);
         }
      }
   }
}
