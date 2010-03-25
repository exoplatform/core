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
package org.exoplatform.services.security.j2ee.websphere;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SAS.
 * 
 * WebsphereFilter for removing cookie when it necessary.
 * 
 * @author <a href="mailto:alexey.zavizionov@exoplatform.com.ua">Alexey
 *         Zavizionov</a>
 * @version $Id: $ Mar 4, 2008
 */
public class WebsphereFilter implements Filter
{

   /**
    * Exo logger.
    */
   private Log log = ExoLogger.getLogger("exo.core.component.security.core.WebsphereFilter");

   /**
    * First ltpa cookie token name.
    */
   private static final String cookieName = "LtpaToken";

   /**
    * Second ltpa cookie token name.
    */
   private static final String cookieName2 = "LtpaToken2";

   /**
    * Destroy.
    */
   public void destroy()
   {
   }

   /**
    * Do filter. Remove ltpa token cookie when we are going on public context, nothing to do otherwise.
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException
   {
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      HttpServletResponse httpResponse = (HttpServletResponse)response;
      if (httpRequest.getQueryString() == null && httpRequest.getRequestURI() != null
         && httpRequest.getRequestURI().contains("/public"))
      {
         removeLtpaTokenCookie(httpRequest, httpResponse);
      }
      else if (httpRequest.getQueryString() != null && httpRequest.getQueryString().contains("UIPortalComponentLogin")
         && httpRequest.getRequestURI() != null && httpRequest.getRequestURI().contains("/public"))
      {
         removeLtpaTokenCookie(httpRequest, httpResponse);
      }
      chain.doFilter(request, response);
   }

   /**
    * Initialization.
    */
   public void init(FilterConfig filterConfig) throws ServletException
   {
   }

   /**
    * Remove ltpa token cookies.
    * 
    * @param req HttpServletRequest
    * @param res HttpServletResponse
    */
   private void removeLtpaTokenCookie(HttpServletRequest req, HttpServletResponse res)
   {
      Cookie[] cooks = req.getCookies();
      if (cooks != null)
      {
         for (Cookie cook : cooks)
         {
            if (log.isDebugEnabled())
               log.debug("WebsphereFilter.removeLtpaTokenCookie() cook.getName() = " + cook.getName());
            if (cook != null
               && (cookieName.equalsIgnoreCase(cook.getName()) || cookieName2.equalsIgnoreCase(cook.getName())))
            {
               cook.setMaxAge(0);
               cook.setPath("/");
               res.addCookie(cook);
               if (log.isDebugEnabled())
                  log.debug("WebsphereFilter.removeLtpaTokenCookie() REMOVED LtpaToken = ");
            }
         }
      }
   }

}
