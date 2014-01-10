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
package org.exoplatform.services.organization;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.List;

public class OrganizationDatabaseInitializer extends BaseComponentPlugin implements OrganizationServiceInitializer,
   ComponentPlugin
{

   protected static final Log LOG = ExoLogger
      .getLogger("exo.core.component.organization.api.OrganizationDatabaseInitializer");

   private OrganizationConfig config_;

   protected static final int CHECK_EMPTY = 0, CHECK_ENTRY = 1;

   private int checkDatabaseAlgorithm_ = CHECK_EMPTY;

   private boolean printInfo_;

   private boolean updateUsers_;

   public OrganizationDatabaseInitializer(InitParams params) throws Exception
   {
      String checkConfig = params.getValueParam("checkDatabaseAlgorithm").getValue();
      if (checkConfig.trim().equalsIgnoreCase("entry"))
      {
         checkDatabaseAlgorithm_ = CHECK_ENTRY;
      }
      else
      {
         checkDatabaseAlgorithm_ = CHECK_EMPTY;
      }
      String printInfoConfig = params.getValueParam("printInformation").getValue();
      printInfo_ = printInfoConfig.trim().equalsIgnoreCase("true");
      ValueParam usParam = params.getValueParam("updateUsers");
      if (usParam != null)
      {
         String updateUsersParam = usParam.getValue();
         updateUsers_ = (updateUsersParam != null && updateUsersParam.trim().equalsIgnoreCase("true"));
      }
      config_ = params.getObjectParamValues(OrganizationConfig.class).get(0);
   }

   public void init(OrganizationService service) throws Exception
   {
      if (checkDatabaseAlgorithm_ == CHECK_EMPTY && checkExistDatabase(service))
      {
         return;
      }
      String alg = "check empty database";
      if (checkDatabaseAlgorithm_ == CHECK_ENTRY)
         alg = "check entry database";
      printInfo("=======> Initialize the  organization service data  using algorithm " + alg);
      createGroups(service);
      createMembershipTypes(service);
      createUsers(service);
      printInfo("<=======");
   }

   protected boolean checkExistDatabase(OrganizationService service) throws Exception
   {
      PageList<?> users = service.getUserHandler().getUserPageList(10);
      if (users != null && users.getAvailable() > 0)
         return true;
      return false;
   }

   protected void createGroups(OrganizationService orgService) throws Exception
   {
      printInfo("  Init  Group Data");
      List<?> groups = config_.getGroup();
      for (int i = 0; i < groups.size(); i++)
      {
         OrganizationConfig.Group data = (OrganizationConfig.Group)groups.get(i);
         String groupId = null;
         String parentId = data.getParentId();
         if (parentId == null || parentId.length() == 0)
            groupId = "/" + data.getName();
         else
            groupId = data.getParentId() + "/" + data.getName();

         if (orgService.getGroupHandler().findGroupById(groupId) == null)
         {
            Group group = orgService.getGroupHandler().createGroupInstance();
            group.setGroupName(data.getName());
            group.setDescription(data.getDescription());
            group.setLabel(data.getLabel());
            if (parentId == null || parentId.length() == 0)
            {
               orgService.getGroupHandler().addChild(null, group, true);
            }
            else
            {
               Group parentGroup = orgService.getGroupHandler().findGroupById(parentId);
               orgService.getGroupHandler().addChild(parentGroup, group, true);
            }
            printInfo("    Create Group " + groupId);
         }
         else
         {
            printInfo("    Group " + groupId + " already exists, ignoring the entry");
         }
      }
   }

   protected void createMembershipTypes(OrganizationService service) throws Exception
   {
      printInfo("  Init  Membership Type  Data");
      List<?> types = config_.getMembershipType();
      for (int i = 0; i < types.size(); i++)
      {
         OrganizationConfig.MembershipType data = (OrganizationConfig.MembershipType)types.get(i);
         if (service.getMembershipTypeHandler().findMembershipType(data.getType()) == null)
         {
            MembershipType type = service.getMembershipTypeHandler().createMembershipTypeInstance();
            type.setName(data.getType());
            type.setDescription(data.getDescription());
            service.getMembershipTypeHandler().createMembershipType(type, true);
            printInfo("    Created Membership Type " + data.getType());
         }
         else
         {
            printInfo("    Membership Type " + data.getType() + " already exists, ignoring the entry");
         }
      }
   }

   protected void createUsers(OrganizationService service) throws Exception
   {
      printInfo("  Init  User  Data");
      List<?> users = config_.getUser();
      MembershipHandler mhandler = service.getMembershipHandler();
      for (int i = 0; i < users.size(); i++)
      {
         OrganizationConfig.User data = (OrganizationConfig.User)users.get(i);
         UserHandler handler = service.getUserHandler();
         User user = handler.findUserByName(data.getUserName(), UserStatus.BOTH);
         if (user == null)
         {
            user = handler.createUserInstance(data.getUserName());
            user.setPassword(data.getPassword());
            user.setFirstName(data.getFirstName());
            user.setLastName(data.getLastName());
            user.setEmail(data.getEmail());
            user.setDisplayName(data.getDisplayName());
            handler.createUser(user, true);
            if (!data.isEnabled())
            {
                handler.setEnabled(user.getUserName(), false, true);    
            }
            printInfo("    Created user " + data.getUserName());
         } 
         else if (updateUsers_) 
         {
            if (!user.isEnabled())
            {
               handler.setEnabled(user.getUserName(), true, true);
            }
            handler.saveUser(user, true);
            if (!data.isEnabled())
            {
               handler.setEnabled(user.getUserName(), false, true);
            }
            printInfo("    User " + data.getUserName() + " updated");
         }
         else
         {
            printInfo("    User " + data.getUserName() + " already exists, ignoring the entry");
         }

         String groups = data.getGroups();
         String[] entry = groups.split(",");
         for (int j = 0; j < entry.length; j++)
         {
            String[] temp = entry[j].trim().split(":");
            String membership = temp[0];
            String groupId = temp[1];
            if (mhandler.findMembershipByUserGroupAndType(data.getUserName(), groupId, membership) == null)
            {
               Group group = service.getGroupHandler().findGroupById(groupId);
               MembershipType mt = service.getMembershipTypeHandler().createMembershipTypeInstance();
               mt.setName(membership);
               mhandler.linkMembership(user, group, mt, true);
               printInfo("    Created membership " + data.getUserName() + ", " + groupId + ", " + membership);
            }
            else
            {
               printInfo("    Ignored membership " + data.getUserName() + ", " + groupId + ", " + membership);
            }
         }
      }
   }

   protected void printInfo(String message)
   {
      if (printInfo_)
         LOG.info(message);
   }

   /**
    * @return the config
    */
   protected OrganizationConfig getConfig()
   {
      return config_;
   }

   /**
    * @return the checkDatabaseAlgorithm
    */
   protected int getCheckDatabaseAlgorithm()
   {
      return checkDatabaseAlgorithm_;
   }

   /**
    * @return the printInfo
    */
   protected boolean isPrintInfo()
   {
      return printInfo_;
   }

   /**
    * @return the updateUsers
    */
   public boolean isUpdateUsers()
   {
      return updateUsers_;
   }
}
