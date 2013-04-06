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

import org.exoplatform.container.component.ComponentPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Aug 22, 2003 Time: 4:46:04 PM
 * Concept: The eXo platform organization has 5 main components: user , user profile, group,
 * membership type and membership.
 * 
 * <pre>
 * |-----------------|    |---------------|  |---------------|    |---------------|
 * |                 |    |               |  |               |    |               |
 * |                 |    |               |  |               |==| |               | 
 * |      USER       |&lt;=&gt; | USER PROFILE  |  |     GROUP     |  | |MEMBERSHIP TYPE|
 * |                 |    |               |  |               |&lt;=| |               |
 * |                 |    |               |  |               |    |               |
 * |---------------- |    |---------------|  |---------------|    |---------------|
 *              \                                  /            /
 *               \                                /            /
 *                \                              /            /
 *                 \                            /            /
 *                  \                          /            /
 *                   \                        /            /
 *                    \ |----------------------------------|
 *                      |                                  |
 *                      |            MEMBERSHIP            |
 *                      |     The membership hold the      |
 *                      |     relation of the user, group  |
 *                      |     and membership type          |
 *                      |                                  |
 *                      |--------------------------------- |
 * </pre>
 * 
 * The user component contain and manage the basic information of an user such
 * the username , password, first name, last name, email.. The user profile
 * component contain and manage the extra user information such the user
 * personal information, business information.. The third party developers can
 * also add the information of an user for their application use. The group
 * component contains and manage a tree of the groups. The membership type
 * contains and manage a list of the predefined membership The membership
 * component contains and manage the relation of the user , group and membership
 * type. An user can have one or more membership in a group, for example: user A
 * can have the 'member' and 'admin' membership in group /user. An user is in a
 * group if he has at least one membership in that group. This is the main
 * interface of the organization service. From this interface, the developer can
 * access the sub interface UserHandler to manage the user, UserProfile handler
 * to manage the user profile, GroupHandler to manage the group and the
 * MembershipHandler to manage the user group and membership relation.
 * @LevelAPI Platform
 */
public interface OrganizationService
{
   /**
    * This method return an UserHandler object that use to manage the user
    * operation such create, update , delete , find user.
    * 
    * @see UserHandler
    * @return a UserHandler object.
    **/
   public UserHandler getUserHandler();

   /**
    * @return a UserProfileHandler object that use to manage the information of
    *         the user
    * @see UserProfileHandler
    */
   public UserProfileHandler getUserProfileHandler();

   /**
    * @return return an GroupHandler implementation instance.
    * @see GroupHandler
    */
   public GroupHandler getGroupHandler();

   /**
    * @return return a MembershipTypeHandler implementation instance
    * @see MembershipTypeHandler
    */
   public MembershipTypeHandler getMembershipTypeHandler();

   /**
    * @return return a MembershipHandler implementation instance
    * @see MembershipHandler
    */
   public MembershipHandler getMembershipHandler();

   /**
    * Use this method to register an listener to the UserHandler, GroupHandler or
    * MembershipHandler. The listener must be and instance of @see
    * UserEventistener ,
    * 
    * @see GroupEventListener or @see MembershipEventListener.
    * @param listener A customized listener instance
    * @throws Exception
    */
   public void addListenerPlugin(ComponentPlugin listener) throws Exception;
}
