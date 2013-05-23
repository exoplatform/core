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

import org.apache.directory.shared.ldap.message.ArrayNamingEnumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;

/**
 * @author <a href="mailto:dmi3.kuleshov@gmail.com">Dmitry Kuleshov</a>
 */
public class DummyLdapContext extends InitialDirContext implements LdapContext
{
   InitialContext initialContext;

   DirContext appRoot;

   public DummyLdapContext(InitialContext initialContext) throws NamingException
   {
      this.initialContext = initialContext;
      appRoot = (DirContext)initialContext.lookup("");
   }

   @Override
   public Attributes getAttributes(Name name) throws NamingException
   {
      return appRoot.getAttributes(name);
   }

   @Override
   public Attributes getAttributes(String name) throws NamingException
   {
      return appRoot.getAttributes(name);
   }

   @Override
   public Attributes getAttributes(Name name, String[] attrIds) throws NamingException
   {
      return appRoot.getAttributes(name, attrIds);
   }

   @Override
   public Attributes getAttributes(String name, String[] attrIds) throws NamingException
   {
      return appRoot.getAttributes(name, attrIds);
   }

   @Override
   public void modifyAttributes(Name name, int mod_op, Attributes attrs) throws NamingException
   {
      appRoot.modifyAttributes(name, mod_op, attrs);
   }

   @Override
   public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException
   {
      appRoot.modifyAttributes(name, mod_op, attrs);
   }

   @Override
   public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException
   {
      appRoot.modifyAttributes(name, mods);
   }

   @Override
   public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException
   {
      appRoot.modifyAttributes(name, mods);
   }

   @Override
   public void bind(Name name, Object obj, Attributes attrs) throws NamingException
   {
      appRoot.bind(name, obj, attrs);
   }

   @Override
   public void bind(String name, Object obj, Attributes attrs) throws NamingException
   {
      appRoot.bind(name, obj, attrs);
   }

   @Override
   public void rebind(Name name, Object obj, Attributes attrs) throws NamingException
   {
      appRoot.rebind(name, obj, attrs);
   }

   @Override
   public void rebind(String name, Object obj, Attributes attrs) throws NamingException
   {
      appRoot.rebind(name, obj, attrs);
   }

   @Override
   public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException
   {
      return appRoot.createSubcontext(name, attrs);
   }

   @Override
   public DirContext createSubcontext(String name, Attributes attrs) throws NamingException
   {
      return appRoot.createSubcontext(name, attrs);
   }

   @Override
   public DirContext getSchema(Name name) throws NamingException
   {
      return appRoot.getSchema(name);
   }

   @Override
   public DirContext getSchema(String name) throws NamingException
   {
      return appRoot.getSchema(name);
   }

   @Override
   public DirContext getSchemaClassDefinition(Name name) throws NamingException
   {
      return appRoot.getSchemaClassDefinition(name);
   }

   @Override
   public DirContext getSchemaClassDefinition(String name) throws NamingException
   {
      return appRoot.getSchemaClassDefinition(name);
   }

   @Override
   public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
      throws NamingException
   {
      return appRoot.search(name, matchingAttributes, attributesToReturn);
   }

   @Override
   public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn)
      throws NamingException
   {
      return appRoot.search(name, matchingAttributes, attributesToReturn);
   }

   @Override
   public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException
   {
      return appRoot.search(name, matchingAttributes);
   }

   @Override
   public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException
   {
      return appRoot.search(name, matchingAttributes);
   }

   @Override
   public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons) throws NamingException
   {
      return appRoot.search(name, filter, cons);
   }

   @Override
   public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
      throws NamingException
   {
      if (!filter.startsWith("("))
      {
         filter = "(" + filter + ")";
      }

      return swapNameWithNameInNamespace(appRoot.search(name, filter, cons));
   }

   @Override
   public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs, SearchControls cons)
      throws NamingException
   {
      return appRoot.search(name, filterExpr, filterArgs, cons);
   }

   @Override
   public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
      SearchControls cons) throws NamingException
   {
      return appRoot.search(name, filterExpr, filterArgs, cons);
   }

   @Override
   public Object lookup(Name name) throws NamingException
   {
      return initialContext.lookup(name);
   }

   @Override
   public Object lookup(String name) throws NamingException
   {
      return initialContext.lookup(name);
   }

   @Override
   public void bind(Name name, Object obj) throws NamingException
   {
      appRoot.bind(name, obj);
   }

   @Override
   public void bind(String name, Object obj) throws NamingException
   {
      appRoot.bind(name, obj);
   }

   @Override
   public void rebind(Name name, Object obj) throws NamingException
   {
      appRoot.rebind(name, obj);
   }

   @Override
   public void rebind(String name, Object obj) throws NamingException
   {
      appRoot.rebind(name, obj);
   }

   @Override
   public void unbind(Name name) throws NamingException
   {
      appRoot.unbind(name);
   }

   @Override
   public void unbind(String name) throws NamingException
   {
      appRoot.unbind(name);
   }

   @Override
   public void rename(Name oldName, Name newName) throws NamingException
   {
      appRoot.rename(oldName, newName);
   }

   @Override
   public void rename(String oldName, String newName) throws NamingException
   {
      appRoot.rename(oldName, newName);
   }

   @Override
   public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
   {
      return appRoot.list(name);
   }

   @Override
   public NamingEnumeration<NameClassPair> list(String name) throws NamingException
   {
      return appRoot.list(name);
   }

   @Override
   public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
   {
      return appRoot.listBindings(name);
   }

   @Override
   public NamingEnumeration<Binding> listBindings(String name) throws NamingException
   {
      return appRoot.listBindings(name);
   }

   @Override
   public void destroySubcontext(Name name) throws NamingException
   {
      appRoot.destroySubcontext(name);
   }

   @Override
   public void destroySubcontext(String name) throws NamingException
   {
      appRoot.destroySubcontext(name);
   }

   @Override
   public Context createSubcontext(Name name) throws NamingException
   {
      return appRoot.createSubcontext(name);
   }

   @Override
   public Context createSubcontext(String name) throws NamingException
   {
      return appRoot.createSubcontext(name);
   }

   @Override
   public Object lookupLink(Name name) throws NamingException
   {
      return initialContext.lookupLink(name);
   }

   @Override
   public Object lookupLink(String name) throws NamingException
   {
      return initialContext.lookupLink(name);
   }

   @Override
   public NameParser getNameParser(Name name) throws NamingException
   {
      return initialContext.getNameParser(name);
   }

   @Override
   public NameParser getNameParser(String name) throws NamingException
   {
      return initialContext.getNameParser(name);
   }

   @Override
   public Name composeName(Name name, Name prefix) throws NamingException
   {

      return appRoot.composeName(name, prefix);
   }

   @Override
   public String composeName(String name, String prefix) throws NamingException
   {
      return appRoot.composeName(name, prefix);
   }

   @Override
   public Object addToEnvironment(String propName, Object propVal) throws NamingException
   {
      return appRoot.addToEnvironment(propName, propVal);
   }

   @Override
   public Object removeFromEnvironment(String propName) throws NamingException
   {
      return appRoot.removeFromEnvironment(propName);
   }

   @Override
   public Hashtable<?, ?> getEnvironment() throws NamingException
   {
      return appRoot.getEnvironment();
   }

   @Override
   public void close() throws NamingException
   {
      appRoot.close();
      initialContext.close();
   }

   @Override
   public String getNameInNamespace() throws NamingException
   {
      return appRoot.getNameInNamespace();
   }

   public ExtendedResponse extendedOperation(ExtendedRequest request) throws NamingException
   {
      return null;
   }

   public LdapContext newInstance(Control[] requestControls) throws NamingException
   {
      return null;
   }

   public void reconnect(Control[] connCtls) throws NamingException
   {
   }

   public Control[] getConnectControls() throws NamingException
   {
      return null;
   }

   public void setRequestControls(Control[] requestControls) throws NamingException
   {
   }

   public Control[] getRequestControls() throws NamingException
   {
      return null;
   }

   public Control[] getResponseControls() throws NamingException
   {
      return null;
   }

   /**
    * Utility method cut off the "ou=groups,ou=portal" string
    * to avoid it to be used twice
    * @param name
    * @return
    */
   private String removeGroupProtal(String name)
   {
      int i = name.toLowerCase().indexOf("ou=groups,ou=portal");
      if (i > -1)
      {
         int index = name.substring(0, i).lastIndexOf(',');
         return name.substring(0, index > 0 ? index : i);
      }
      return name;
   }

   @SuppressWarnings("unchecked")
   private NamingEnumeration<SearchResult> swapNameWithNameInNamespace(NamingEnumeration<SearchResult> nesr) throws NamingException
   {
      List<SearchResult> resultList = new ArrayList<SearchResult>();
      SearchResult sr;

      while (nesr.hasMore())
      {
         sr = nesr.next();
         sr.setNameInNamespace(sr.getName());
         sr.setName(removeGroupProtal(sr.getName()));

         resultList.add(sr);
      }

      return new ArrayNamingEnumeration(resultList.toArray());
   }

}
