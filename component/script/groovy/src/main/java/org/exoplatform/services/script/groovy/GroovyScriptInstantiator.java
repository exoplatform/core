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
package org.exoplatform.services.script.groovy;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilationFailedException;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.script.groovy.jarjar.JarJarClassLoader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class GroovyScriptInstantiator
{

   /** Our logger. */
   private static final Log LOG = ExoLogger.getLogger(GroovyScriptInstantiator.class.getName());

   /**
    * eXo Container.
    */
   private ExoContainer container;

   /** The global mapping. */
   private Map<String, String> mapping;

   /**
    * @param containerContext container context
    */
   public GroovyScriptInstantiator(ExoContainerContext containerContext)
   {
      this.container = containerContext.getContainer();
      this.mapping = Collections.synchronizedMap(new HashMap<String, String>());
   }

   /**
    * Load script from given address.
    * 
    * @param spec the resource's address.
    * @return the object created from groovy script.
    * @throws MalformedURLException if parameter <code>url</code> have wrong
    *           format.
    * @throws IOException if can't load script from given <code>url</code>.
    * @see GroovyScriptInstantiator#instantiateScript(URL)
    * @see GroovyScriptInstantiator#instantiateScript(InputStream)
    */
   public Object instantiateScript(String spec) throws MalformedURLException, IOException
   {
      return instantiateScript(new URL(spec));
   }

   /**
    * Load script from given address.
    * 
    * @param url the resource's address.
    * @return the object created from groovy script.
    * @throws IOException if can't load script from given <code>url</code>.
    * @see GroovyScriptInstantiator#instantiateScript(InputStream)
    */
   public Object instantiateScript(URL url) throws IOException
   {
      String name = url.toString();
      return instantiateScript(new BufferedInputStream(url.openStream()), name);
   }

   /**
    * Parse given stream, the stream must represents groovy script.
    * 
    * @param stream the stream represented groovy script.
    * @return the object created from groovy script.
    * @throws IOException if stream can't be parsed or object can't be created.
    */
   public Object instantiateScript(InputStream stream) throws IOException
   {
      return instantiateScript(stream, null);
   }

   /**
    * Parse given stream, the stream must represents groovy script.
    * 
    * @param stream the stream represented groovy script.
    * @param name script name is null or empty string that groovy completer will
    *          use default name
    * @return the object created from groovy script.
    * @throws IOException if stream can't be parsed or object can't be created.
    */
   public Object instantiateScript(InputStream stream, String name) throws IOException
   {
      GroovyClassLoader loader;
      if (mapping.size() > 0)
      {
         JarJarClassLoader jarjarLoader = new JarJarClassLoader();
         jarjarLoader.addMapping(mapping);
         loader = jarjarLoader;
      }
      else
      {
         loader = new GroovyClassLoader();
      }
      Class<?> clazz = null;
      try
      {
         if (name != null && name.length() > 0)
            clazz = loader.parseClass(stream, name);
         else
            clazz = loader.parseClass(stream);
      }
      catch (CompilationFailedException e)
      {
         throw new IOException("Error occurs when parse stream, compiler error:\n " + e.getMessage());
      }
      try
      {
         return createObject(clazz);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Can't instantiate groovy script: " + e.getMessage(), e);
      }
      finally
      {
         stream.close();
      }
   }

   /**
    * Created object from given class, if class has parameters in constructor,
    * then this parameters will be searched in container.
    * 
    * @param clazz java-groovy class
    */
   private Object createObject(Class<?> clazz) throws Exception
   {

      Constructor<?>[] constructors = clazz.getConstructors();

      /*
       * Sort constructors by number of parameters. With more parameters must be
       * first.
       */
      Arrays.sort(constructors, COMPARATOR);

      l : for (Constructor<?> c : constructors)
      {
         Class<?>[] parameterTypes = c.getParameterTypes();
         if (parameterTypes.length == 0)
            return c.newInstance();

         List<Object> parameters = new ArrayList<Object>(parameterTypes.length);

         for (Class<?> parameterType : parameterTypes)
         {
            Object param = container.getComponentInstanceOfType(parameterType);
            if (param == null)
               continue l;
            parameters.add(param);
         }

         return c.newInstance(parameters.toArray(new Object[parameters.size()]));
      }
      return null;

   }

   private static final ConstructorsComparator COMPARATOR = new ConstructorsComparator();

   /**
    * Sorts array of constructors by number of parameters.
    */
   private static class ConstructorsComparator implements Comparator<Constructor<?>>
   {

      /**
       * {@inheritDoc}
       */
      public int compare(Constructor<?> constructor1, Constructor<?> constructor2)
      {
         int c1 = constructor1.getParameterTypes().length;
         int c2 = constructor2.getParameterTypes().length;
         if (c1 < c2)
            return 1;
         if (c1 > c2)
            return -1;
         return 0;
      }

   }

   public void addPlugin(ComponentPlugin plugin)
   {
      if (plugin instanceof GroovyScriptJarJarPlugin)
      {
         GroovyScriptJarJarPlugin jarjarPlugin = (GroovyScriptJarJarPlugin)plugin;
         LOG.debug("Add mapping to groovy instantiator:" + jarjarPlugin.getMapping());
         mapping.putAll(jarjarPlugin.getMapping());
      }
   }

}
