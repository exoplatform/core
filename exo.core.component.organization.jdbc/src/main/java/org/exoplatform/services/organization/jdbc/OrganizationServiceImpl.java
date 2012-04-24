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
package org.exoplatform.services.organization.jdbc;

import org.exoplatform.services.database.DBObjectMapper;
import org.exoplatform.services.database.DBTableManager;
import org.exoplatform.services.database.DatabaseService;
import org.exoplatform.services.database.ExoDatasource;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.BaseOrganizationService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Aug 22, 2003 Time: 4:51:21 PM
 */
public class OrganizationServiceImpl extends BaseOrganizationService
{

   public OrganizationServiceImpl(ListenerService listenerService, DatabaseService dbService) throws Exception
   {
      ExoDatasource datasource = dbService.getDatasource();
      userDAO_ = new UserDAOImpl(this, listenerService, datasource, new UserMapper());
      groupDAO_ = new GroupDAOImpl(this, listenerService, datasource, new GroupMapper());
      membershipTypeDAO_ = new MembershipTypeDAOImpl(listenerService, datasource, new MembershipTypeMapper());

      membershipDAO_ = new MembershipDAOImpl(listenerService, datasource, new MembershipMapper(), this);
      userProfileDAO_ = new UserProfileDAOImpl(listenerService, datasource, new UserProfileMapper(), userDAO_);

      DBTableManager dbManager = datasource.getDBTableManager();
      if (!dbManager.hasTable(UserImpl.class))
         dbManager.createTable(UserImpl.class, false);

      if (!dbManager.hasTable(GroupImpl.class))
         dbManager.createTable(GroupImpl.class, false);
      if (!dbManager.hasTable(MembershipTypeImpl.class))
         dbManager.createTable(MembershipTypeImpl.class, false);
      if (!dbManager.hasTable(UserProfileData.class))
         dbManager.createTable(UserProfileData.class, false);
      if (!dbManager.hasTable(MembershipImpl.class))
         dbManager.createTable(MembershipImpl.class, false);
   }

   static class UserMapper implements DBObjectMapper<UserImpl>
   {

      public String[][] toParameters(UserImpl bean) throws Exception
      {
         Date date = bean.getCreatedDate();
         if (date == null)
            date = Calendar.getInstance().getTime();
         java.sql.Date createdDate = new java.sql.Date(date.getTime());

         date = bean.getLastLoginTime();
         if (date == null)
            date = Calendar.getInstance().getTime();
         java.sql.Date lastLogin = new java.sql.Date(date.getTime());
         return new String[][]{{"ID", String.valueOf(bean.getDBObjectId())}, {"USER_NAME", bean.getUserName()},
            {"PASSWORD", bean.getPassword()}, {"FIRST_NAME", bean.getFirstName()}, {"LAST_NAME", bean.getLastName()},
            {"EMAIL", bean.getEmail()}, {"DISPLAY_NAME", bean.getDisplayName()},
            {"CREATED_DATE", createdDate.toString()}, {"LAST_LOGIN_TIME", lastLogin.toString()},
            {"ORGANIZATION_ID", bean.getOrganizationId()}};
      }

      public void mapUpdate(UserImpl bean, PreparedStatement statement) throws Exception
      {
         statement.setString(1, bean.getUserName());
         statement.setString(2, bean.getPassword());
         statement.setString(3, bean.getFirstName());
         statement.setString(4, bean.getLastName());
         statement.setString(5, bean.getEmail());
         statement.setString(6, bean.getDisplayName());

         Date createdDate = bean.getCreatedDate();
         if (createdDate == null)
            createdDate = Calendar.getInstance().getTime();
         statement.setDate(7, new java.sql.Date(createdDate.getTime()));

         Date lastLoginTime = bean.getLastLoginTime();
         if (lastLoginTime == null)
            lastLoginTime = Calendar.getInstance().getTime();
         statement.setDate(8, new java.sql.Date(lastLoginTime.getTime()));

         statement.setString(9, bean.getOrganizationId());
      }

      public void mapResultSet(ResultSet res, UserImpl bean) throws Exception
      {
         bean.setDBObjectId(res.getLong("ID"));
         bean.setUserName(res.getString("USER_NAME"));
         bean.setPassword(res.getString("PASSWORD"));
         bean.setFirstName(res.getString("FIRST_NAME"));
         bean.setLastName(res.getString("LAST_NAME"));
         bean.setEmail(res.getString("EMAIL"));
         bean.setDisplayName(res.getString("DISPLAY_NAME"));

         Calendar calendar = Calendar.getInstance();
         res.getDate("CREATED_DATE", calendar);
         bean.setCreatedDate(calendar.getTime());

         res.getDate("LAST_LOGIN_TIME", calendar);
         bean.setLastLoginTime(calendar.getTime());

         bean.setOrganizationId(res.getString("ORGANIZATION_ID"));
      }
   }

   static class GroupMapper implements DBObjectMapper<GroupImpl>
   {

      public String[][] toParameters(GroupImpl bean) throws Exception
      {
         return new String[][]{{"GROUP_ID", bean.getId()}, {"PARENT_ID", bean.getParentId()},
            {"GROUP_NAME", bean.getGroupName()}, {"LABEL", bean.getLabel()}, {"GROUP_DESC", bean.getDescription()}};
      }

      public void mapUpdate(GroupImpl bean, PreparedStatement statement) throws Exception
      {
         statement.setString(1, bean.getId());
         statement.setString(2, bean.getParentId());
         statement.setString(3, bean.getGroupName());
         statement.setString(4, bean.getLabel());
         statement.setString(5, bean.getDescription());
      }

      public void mapResultSet(ResultSet res, GroupImpl bean) throws Exception
      {
         bean.setDBObjectId(res.getLong("ID"));
         bean.setId(res.getString("GROUP_ID"));
         bean.setParentId(res.getString("PARENT_ID"));
         bean.setGroupName(res.getString("GROUP_NAME"));
         bean.setLabel(res.getString("LABEL"));
         bean.setDescription(res.getString("GROUP_DESC"));
      }
   }

   static class MembershipTypeMapper implements DBObjectMapper<MembershipTypeImpl>
   {

      public String[][] toParameters(MembershipTypeImpl bean) throws Exception
      {
         Date date = bean.getCreatedDate();
         if (date == null)
            date = Calendar.getInstance().getTime();
         java.sql.Date createdDate = new java.sql.Date(date.getTime());

         date = bean.getModifiedDate();
         if (date == null)
            date = Calendar.getInstance().getTime();
         java.sql.Date modifiedDate = new java.sql.Date(date.getTime());
         return new String[][]{{"MT_NAME", bean.getName()}, {"MT_OWNER", bean.getOwner()},
            {"MT_DESCRIPTION", bean.getDescription()}, {"CREATED_DATE", createdDate.toString()},
            {"LAST_LOGIN_TIME", modifiedDate.toString()}};
      }

      public void mapUpdate(MembershipTypeImpl bean, PreparedStatement statement) throws Exception
      {
         statement.setString(1, bean.getName());
         statement.setString(2, bean.getOwner());
         statement.setString(3, bean.getDescription());

         Date createdDate = bean.getCreatedDate();
         if (createdDate == null)
            createdDate = Calendar.getInstance().getTime();
         statement.setDate(4, new java.sql.Date(createdDate.getTime()));

         Date lastLoginTime = bean.getModifiedDate();
         if (lastLoginTime == null)
            lastLoginTime = Calendar.getInstance().getTime();
         statement.setDate(5, new java.sql.Date(lastLoginTime.getTime()));
      }

      public void mapResultSet(ResultSet res, MembershipTypeImpl bean) throws Exception
      {
         bean.setDBObjectId(res.getLong("ID"));
         bean.setName(res.getString("MT_NAME"));
         bean.setOwner(res.getString("MT_OWNER"));
         bean.setDescription(res.getString("MT_DESCRIPTION"));

         Calendar calendar = Calendar.getInstance();
         res.getDate("CREATED_DATE", calendar);
         bean.setCreatedDate(calendar.getTime());

         res.getDate("MODIFIED_DATE", calendar);
         bean.setModifiedDate(calendar.getTime());
      }
   }

   static class MembershipMapper implements DBObjectMapper<MembershipImpl>
   {

      public String[][] toParameters(MembershipImpl bean) throws Exception
      {
         return new String[][]{{"MEMBERSHIP_ID", bean.getId()}, {"MEMBERSHIP_TYPE", bean.getMembershipType()},
            {"GROUP_ID", bean.getGroupId()}, {"USER_NAME", bean.getUserName()}};
      }

      public void mapUpdate(MembershipImpl bean, PreparedStatement statement) throws Exception
      {
         statement.setString(1, bean.getId());
         statement.setString(2, bean.getMembershipType());
         statement.setString(3, bean.getGroupId());
         statement.setString(4, bean.getUserName());
      }

      public void mapResultSet(ResultSet res, MembershipImpl bean) throws Exception
      {
         bean.setDBObjectId(res.getLong("ID"));
         bean.setId(res.getString("MEMBERSHIP_ID"));
         bean.setMembershipType(res.getString("MEMBERSHIP_TYPE"));
         bean.setGroupId(res.getString("GROUP_ID"));
         bean.setUserName(res.getString("USER_NAME"));
      }

   }

   static class UserProfileMapper implements DBObjectMapper<UserProfileData>
   {

      public String[][] toParameters(UserProfileData bean) throws Exception
      {
         return new String[][]{{"USER_NAME", bean.getUserName()}, {"PROFILE", bean.getProfile()}};
      }

      public void mapUpdate(UserProfileData bean, PreparedStatement statement) throws Exception
      {
         statement.setString(1, bean.getUserName());
         statement.setString(2, bean.getProfile());
      }

      public void mapResultSet(ResultSet res, UserProfileData bean) throws Exception
      {
         bean.setDBObjectId(res.getLong("ID"));
         bean.setUserName(res.getString("USER_NAME"));
         bean.setProfile(res.getString("PROFILE"));
      }

   }

}
