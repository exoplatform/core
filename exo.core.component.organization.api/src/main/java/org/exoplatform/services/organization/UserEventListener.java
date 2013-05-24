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
 * If the other service or a third party want to customize their code to handle
 * an user event, the event can be new , update or remove. They should create a class that extends this class
 * and register the listener to the organization service.
 * There are 2 ways to register a listener to the
 * service. a) To do it programatically: [..] import
 * org.exoplatform.container.PortalContainer ; import
 * org.exoplatform.services.organization.OrganizationService ; [..]
 * UserEventListener listener = new MyUserEventListener(..) ; PortalContainer
 * pcontainer = PortalContainer.getInstance() ; OrganizationService service =
 * (OrganizationService) pcontainer.getInstanceOfType(OrganizationService.class)
 * ; service.getUserHandler().addUserEventListener(listener) ; b) Register by
 * the xml configuration: You need to create a my.package.MyUserEventListener
 * that extends this class and add a conf/portal/configuration.xml to the
 * classpath. The configuration.xml can be in a jar file. The file should
 * contain the following configuraiton:
 * 
 * <pre>
 * &lt;configuration&gt;
 *   [..]
 *   &lt;external-component-plugins&gt;
 *     &lt;target-component&gt;org.exoplatform.services.organization.OrganizationService&lt;/target-component&gt;
 *     &lt;component-plugin&gt;
 *        &lt;name&gt;my.user.listener&lt;/name&gt;
 *        &lt;set-method&gt;addListenerPlugin&lt;/set-method&gt;
 *        &lt;type&gt;my.package.MyUserEventListener&lt;/type&gt;
 *        &lt;description&gt;your listener description&lt;/description&gt;
 *      &lt;/component-plugin&gt;
 *  &lt;/external-component-plugins&gt;
 *  [...]
 * /configuration&gt;
 * </pre>
 * @author <a href="mailto:tuan08@users.sourceforge.net">Tuan Nguyen</a>
 * @LevelAPI Platform
 */
public class UserEventListener extends BaseComponentPlugin
{
   /**
    * This method is called before the user is persisted to the database.
    * 
    * @param user The user to be saved
    * @param isNew if the user is a new record in the database or not
    * @throws Exception The developer can decide to throw an exception or not. If
    *           the listener throw an exception, the organization service should
    *           not save/update the user to the database
    */
   public void preSave(User user, boolean isNew) throws Exception
   {
   }

   /**
    * This method is called after the user has been saved but not commited yet
    * 
    * @param user The user instance has been saved.
    * @param isNew if the user is a new record in the database or not
    * @throws Exception The developer can decide to throw the exception or not.
    *           If the method throw an exception. The organization service should
    *           role back the data to the state before the method
    *           userHandler.createUser(..) or UserHandler.saveUser(..) is called.
    */
   public void postSave(User user, boolean isNew) throws Exception
   {
   }

   /**
    * This method is called before an user should be deleted
    * 
    * @param user the user to be delete
    * @throws Exception The developer can decide to throw the exception or not.
    *           If the method throw an exception. The organization service should
    *           not remove the user record from the database.
    */
   public void preDelete(User user) throws Exception
   {
   }

   /**
    * This method should be called after the user has been removed from the
    * database but not commited yet.
    * 
    * @param user The user instance which has been removed from the database.
    * @throws Exception The developer can decide to throw the exception or not.
    *           If the method throw the exception, the organization service
    *           should role back the database to the state before the method
    *           UserHandler.removeUser(..) is called.
    */
   public void postDelete(User user) throws Exception
   {
   }
}
