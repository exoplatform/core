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

import java.util.Map;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Aug 21, 2003 Time: 3:22:54 PM This is the
 * interface for a UserProfile data model. The implementor should have an user
 * map info in the implementation. The map should only accept the
 * java.lang.String for the key and the value. Note that after the developer
 * change the user information in the user info map, he need to call
 * 
 * @see UserProfileHandler.saveUserProfile(UserProfile) to persist the change
 */
public interface UserProfile
{
   /**
    * TODO Those keys should be moved to the UserProfile portlet. The
    * organization service should be generic.
    */

   /**
    * The predefine attributes of the user personal info. Those attributes are
    * define in the JSR 168 specification
    */
   final static public String[] PERSONAL_INFO_KEYS =
      {"user.name.given", "user.name.family", "user.name.nickName", "user.bdate", "user.gender", "user.employer",
         "user.department", "user.jobtitle", "user.language",};

   /**
    * The predefine attributes of the user home info. Those attributes are define
    * in the JSR 168 specification
    */
   final static public String[] HOME_INFO_KEYS =
      {"user.home-info.postal.name", "user.home-info.postal.street", "user.home-info.postal.city",
         "user.home-info.postal.stateprov", "user.home-info.postal.postalcode", "user.home-info.postal.country",
         "user.home-info.telecom.mobile.number", "user.home-info.telecom.telephone.number",
         "user.home-info.online.email", "user.home-info.online.uri"};

   /**
    * The predefine attributes of the user businese info. Those attributes are
    * define in the JSR 168 specification
    */
   final static public String[] BUSINESE_INFO_KEYS =
      {"user.business-info.postal.name", "user.business-info.postal.city", "user.business-info.postal.stateprov",
         "user.business-info.postal.postalcode", "user.business-info.postal.country",
         "user.business-info.telecom.mobile.number", "user.business-info.telecom.telephone.number",
         "user.business-info.online.email", "user.business-info.online.uri"};

   /**
    * The predefine attributes of the exoplatform. Those keys are currently used
    * in the exo forum
    */
   final static public String[] OTHER_KEYS = {"user.other-info.avatar.url", "user.other-info.signature",};

   /**
    * @return the username, the identifier of an user profile instance
    */
   public String getUserName();

   /**
    *@deprecated The third party developer should not used this method. We
    *             should pass the username to the @see
    *             UserProfileHandler.createUserInstance() and set the username
    *             for the instance once only.
    */
   public void setUserName(String username);

   /**
    * @return The map that contains the user information. The map should only
    *         accept the java.lang.String for the key and the value.
    */
   public Map<String, String> getUserInfoMap();

   /**
    * @param map The map that contains the extra user information. The map should
    *          contains only the java.lang.String as the key and the value.
    */
   public void setUserInfoMap(Map<String, String> map);

   /**
    * @param attName The key name of an attribute in the user info map.
    * @return null if no key is matched in the user info map or a String value.
    */
   public String getAttribute(String attName);

   /**
    * Use this method to change or add a new attribute to the user info map.
    * 
    * @param key The attribute name of the info
    * @param value An info of the user.
    */
   public void setAttribute(String key, String value);
}
