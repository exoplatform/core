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

import org.exoplatform.container.component.BaseComponentPlugin;

/**
 * If the other service or a third party want to customize their code to handle a
 * MembershipType event, the event can be new , update or remove. They should create a class that extends this class
 * and register the listener to the organization
 * service.
 *
 * <pre>
 * &lt;configuration&gt;
 *   [..]
 *   &lt;external-component-plugins&gt;
 *     &lt;target-component&gt;org.exoplatform.services.organization.OrganizationService&lt;/target-component&gt;
 *     &lt;component-plugin&gt;
 *        &lt;name&gt;my.membership.type.listener&lt;/name&gt;
 *        &lt;set-method&gt;addListenerPlugin&lt;/set-method&gt;
 *        &lt;type&gt;my.package.MyMembershipTypeEventListener&lt;/type&gt;
 *        &lt;description&gt;your listener description&lt;/description&gt;
 *      &lt;/component-plugin&gt;
 *  &lt;/external-component-plugins&gt;
 *  [...]
 * /configuration&gt;
 * </pre>
 * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
 * @LevelAPI Platform
 */
public class MembershipTypeEventListener extends BaseComponentPlugin
{
   /**
    * This method is called before the membership type object is saved.
    * 
    * @param type the membership type to be saved
    * @param isNew If the membership type is a new record in the database or not.
    * @throws Exception The developer can decide to throw the exception or not.
    *           If the method throw an exception. The organization service should
    *           not save the membership type.
    */
   public void preSave(MembershipType type, boolean isNew) throws Exception
   {
   }

   /**
    * This method is called after the membership type has been saved but not commited
    * yet
    * 
    * @param type The mebership type object
    * @param isNew The membership type is a new record or not.
    * @throws Exception The developer can decide to throw the exception or not.
    *           If the method throw an exception. The organization service should
    *           role back the data.
    */
   public void postSave(MembershipType type, boolean isNew) throws Exception
   {
   }

   /**
    * This method is called before the membership type is removed
    * 
    * @param type The membership type object to be removed
    * @throws Exception he developer can decide to throw the exception or not. If
    *           the method throw an exception. The organization service should
    *           not remove the membership type record from the database.
    */
   public void preDelete(MembershipType type) throws Exception
   {
   }

   /**
    * This method should be called after the membership type has been removed from the
    * database but not commited yet.
    * 
    * @param type The membership type which has been removed from the database.
    * @throws Exception The developer can decide to throw the exception or not.
    *           If the method throw the exception, the organization service
    *           should role back the database.
    */
   public void postDelete(MembershipType type) throws Exception
   {
   }
}
