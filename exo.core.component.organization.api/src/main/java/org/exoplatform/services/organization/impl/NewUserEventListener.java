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

import java.util.Date;
import java.util.List;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserProfile;

/**
 * Jul 20, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: NewUserEventListener.java 13079 2007-03-01 15:30:35Z tuan08 $
 */
public class NewUserEventListener extends UserEventListener
{

   private NewUserConfig config_;

   public NewUserEventListener(InitParams params) throws Exception
   {
      config_ = (NewUserConfig)params.getObjectParamValues(NewUserConfig.class).get(0);
   }

   public void preSave(User user, boolean isNew) throws Exception
   {
      if (isNew)
      {
         Date date = new Date();
         user.setLastLoginTime(date);
         user.setCreatedDate(date);
      }
   }

   public void postSave(User user, boolean isNew) throws Exception
   {
      ExoContainer pcontainer = ExoContainerContext.getCurrentContainer();
      OrganizationService service =
         (OrganizationService)pcontainer.getComponentInstanceOfType(OrganizationService.class);
      UserProfile up = service.getUserProfileHandler().createUserProfileInstance();
      up.setUserName(user.getUserName());
      service.getUserProfileHandler().saveUserProfile(up, false);
      if (config_ == null)
         return;
      if (isNew && !config_.isIgnoreUser(user.getUserName()))
      {
         createDefaultUserMemberships(user, service);
      }
   }

   public void preDelete(User user) throws Exception
   {
   }

   public void postDelete(User user) throws Exception
   {
   }

   private void createDefaultUserMemberships(User user, OrganizationService service) throws Exception
   {
      List<?> groups = config_.getGroup();
      if (groups.size() == 0)
         return;
      for (int i = 0; i < groups.size(); i++)
      {
         NewUserConfig.JoinGroup jgroup = (NewUserConfig.JoinGroup)groups.get(i);
         Group group = service.getGroupHandler().findGroupById(jgroup.getGroupId());
         MembershipType mtype = service.getMembershipTypeHandler().findMembershipType(jgroup.getMembership());
         service.getMembershipHandler().linkMembership(user, group, mtype, false);
      }
   }
}
