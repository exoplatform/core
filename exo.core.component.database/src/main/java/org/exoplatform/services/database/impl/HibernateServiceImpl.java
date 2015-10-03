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
package org.exoplatform.services.database.impl;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.database.ObjectQuery;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.io.Serializable;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Tuan Nguyen tuan08@users.sourceforge.net Date: Jun 14, 2003
 * @author dhodnett $Id: HibernateServiceImpl.java,v 1.3 2004/10/30 02:27:52
 *         tuan08 Exp $
 */
public class HibernateServiceImpl implements HibernateService, ComponentRequestLifecycle
{

   public static final String AUTO_DIALECT = "AUTO";

   private static final String USER_PROFILE_DATA_ENTITY_PATH =
           "org.exoplatform.services.organization.impl.UserProfileData";

   private static final String USER_PROFILE_DATA_ENTITY_HSQL_PATH =
           "org.exoplatform.services.organization.impl.UserProfileDataHsql";

   private ThreadLocal<Session> threadLocal_;

   private static final Log LOG = ExoLogger.getLogger("exo.core.component.database.HibernateServiceImpl");

   private HibernateConfigurationImpl conf_;

   private HashSet<String> mappings_ = new HashSet<String>();

   private SessionFactory sessionFactory_;

   public HibernateServiceImpl(InitParams initParams, CacheService cacheService)
   {
      threadLocal_ = new ThreadLocal<Session>();
      PropertiesParam param = initParams.getPropertiesParam("hibernate.properties");
      conf_ = SecurityHelper.doPrivilegedAction(new PrivilegedAction<HibernateConfigurationImpl>()
      {
         public HibernateConfigurationImpl run()
         {
            return new HibernateConfigurationImpl();
         }
      });
      Iterator<?> properties = param.getPropertyIterator();
      while (properties.hasNext())
      {
         Property p = (Property)properties.next();
         conf_.setProperty(p.getName(), p.getValue());
      }

      // Replace the potential "java.io.tmpdir" variable in the connection URL
      String connectionURL = conf_.getProperty("hibernate.connection.url");
      if (connectionURL != null)
      {
         connectionURL =
            connectionURL.replace("${java.io.tmpdir}", PrivilegedSystemHelper.getProperty("java.io.tmpdir"));
         conf_.setProperty("hibernate.connection.url", connectionURL);
      }
   }

   public void addPlugin(ComponentPlugin plugin)
   {
      if (plugin instanceof AddHibernateMappingPlugin)
      {
         AddHibernateMappingPlugin impl = (AddHibernateMappingPlugin)plugin;
         try
         {
            List path = impl.getMapping();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            if (path != null)
            {

               for (int i = 0; i < path.size(); i++)
               {
                  String relativePath = (String)path.get(i);
                  if (!mappings_.contains(relativePath))
                  {
                     mappings_.add(relativePath);
                     URL url = cl.getResource(relativePath);
                     LOG.info("Adding  Hibernate Mapping: " + relativePath);
                     conf_.addURL(url);
                  }
               }
            }

            // Annotations
            List<String> annotations = impl.getAnnotations();
            //Need to create tem SessionFactory in order to get Dialect Informations
            SessionFactory tempSessionFactory = conf_.buildSessionFactory();
            boolean hsqlDialect = ((SessionFactoryImplementor) tempSessionFactory).getDialect() != null &&
                    ((SessionFactoryImplementor) tempSessionFactory).getDialect().toString().contains("HSQL") ?
                    true:false;

            if (tempSessionFactory != null && !tempSessionFactory.isClosed()) {
               tempSessionFactory.close();
            }
            tempSessionFactory = null;
            if (annotations != null)
            {
               for (String annotation : annotations)
               {
                  Class<?> clazz;
                  if ( hsqlDialect && annotation.compareTo(USER_PROFILE_DATA_ENTITY_PATH) == 0 ){
                     clazz = ClassLoading.loadClass(USER_PROFILE_DATA_ENTITY_HSQL_PATH, this);
                     LOG.info("Adding  Hibernate Annotated class: " + USER_PROFILE_DATA_ENTITY_HSQL_PATH);
                  }
                  else {
                     clazz = ClassLoading.loadClass(annotation, this);
                     LOG.info("Adding  Hibernate Annotated class: " + annotation);
                  }
                  conf_.addAnnotatedClass(clazz);
               }

            }
         }
         catch (MappingException ex)
         {
            LOG.error(ex.getLocalizedMessage(), ex);
         }
         catch (ClassNotFoundException ex)
         {
            LOG.error(ex.getLocalizedMessage(), ex);
         }
      }
   }

   public Configuration getHibernateConfiguration()
   {
      return conf_;
   }

   /**
    * @return the SessionFactory
    */
   public SessionFactory getSessionFactory()
   {
      if (sessionFactory_ == null)
      {
         sessionFactory_ = SecurityHelper.doPrivilegedAction(new PrivilegedAction<SessionFactory>()
         {
            public SessionFactory run()
            {
               SessionFactory factory = conf_.buildSessionFactory();
               return factory;
            }
         });
      }

      return sessionFactory_;
   }

   public Session openSession()
   {
      Session currentSession = threadLocal_.get();
      if (currentSession == null)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("open new hibernate session in openSession()");
         }
         currentSession = getSessionFactory().openSession();
         threadLocal_.set(currentSession);
      }
      return currentSession;
   }

   public Session openNewSession()
   {
      Session currentSession = threadLocal_.get();
      if (currentSession != null)
      {
         closeSession(currentSession);
      }
      currentSession = getSessionFactory().openSession();
      threadLocal_.set(currentSession);
      return currentSession;
   }

   public void closeSession(Session session)
   {
      if (session == null)
      {
         return;
      }
      try
      {
         session.close();
         if (LOG.isDebugEnabled())
         {
            LOG.debug("close hibernate session in openSession(Session session)");
         }
      }
      catch (HibernateException t)
      {
         LOG.error("Error closing hibernate session : " + t.getMessage(), t);
      }
      threadLocal_.set(null);
   }

   final public void closeSession()
   {
      Session s = threadLocal_.get();
      if (s != null)
      {
         s.close();
      }
      threadLocal_.set(null);
   }

   public Object findExactOne(Session session, String query, String id) throws Exception
   {
      Object res = session.createQuery(query).setString("id", id).uniqueResult();
      if (res == null)
      {
         throw new ObjectNotFoundException("Cannot find the object with id: " + id);
      }
      return res;
   }

   public Object findOne(Session session, String query, String id) throws Exception
   {
      List<?> l = session.createQuery(query).setString("id", id).list();
      if (l.size() == 0)
      {
         return null;
      }
      else if (l.size() > 1)
      {
         throw new Exception("Expect only one object but found" + l.size());
      }
      else
      {
         return l.get(0);
      }
   }

   public Collection<?> findAll(Session session, String query) throws Exception
   {
      List<?> l = session.createQuery(query).list();
      if (l.size() == 0)
      {
         return null;
      }
      else
      {
         return l;
      }
   }

   @SuppressWarnings("rawtypes")
   public Object findOne(Class clazz, Serializable id) throws Exception
   {
      Session session = openSession();
      Object obj = session.get(clazz, id);
      return obj;
   }

   public Object findOne(ObjectQuery q) throws Exception
   {
      Session session = openSession();
      List<?> l = session.createQuery(q.getHibernateQuery()).list();
      if (l.size() == 0)
      {
         return null;
      }
      else if (l.size() > 1)
      {
         throw new Exception("Expect only one object but found" + l.size());
      }
      else
      {
         return l.get(0);
      }
   }

   public Object create(Object obj) throws Exception
   {
      Session session = openSession();
      session.save(obj);
      session.flush();
      return obj;
   }

   public Object update(Object obj) throws Exception
   {
      Session session = openSession();
      session.update(obj);
      session.flush();
      return obj;
   }

   public Object save(Object obj) throws Exception
   {
      Session session = openSession();
      session.merge(obj);
      session.flush();
      return obj;
   }

   public Object remove(Object obj) throws Exception
   {
      Session session = openSession();
      session.delete(obj);
      session.flush();
      return obj;
   }

   @SuppressWarnings("rawtypes")
   public Object remove(Class clazz, Serializable id) throws Exception
   {
      Session session = openSession();
      Object obj = session.get(clazz, id);
      session.delete(obj);
      session.flush();
      return obj;
   }

   @SuppressWarnings("rawtypes")
   public Object remove(Session session, Class clazz, Serializable id) throws Exception
   {
      Object obj = session.get(clazz, id);
      session.delete(obj);
      return obj;
   }

   public void startRequest(ExoContainer container)
   {

   }

   public void endRequest(ExoContainer container)
   {
      closeSession();
   }
}
