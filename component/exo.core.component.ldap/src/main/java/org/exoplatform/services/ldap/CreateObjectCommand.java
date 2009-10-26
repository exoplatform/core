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
package org.exoplatform.services.ldap;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 16, 2005
 */
public class CreateObjectCommand extends BaseComponentPlugin
{

   private Map<String, Attributes> objectsToCreate;

   @SuppressWarnings("unchecked")
   public CreateObjectCommand(InitParams params)
   {
      objectsToCreate = new HashMap<String, Attributes>();
      Iterator i = params.getPropertiesParamIterator();
      while (i.hasNext())
      {
         PropertiesParam param = (PropertiesParam)i.next();
         Map<String, String> prop = param.getProperties();
         BasicAttributes attrs = new BasicAttributes();
         Iterator entries = prop.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Map.Entry)entries.next();
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            Attribute attr = attrs.get(key);
            if (attr == null)
               attrs.put(new BasicAttribute(key, value));
            else
               attr.add(value);
         }
         objectsToCreate.put(param.getName(), attrs);
      }
   }

   public Map<String, Attributes> getObjectsToCreate()
   {
      return objectsToCreate;
   }

}
