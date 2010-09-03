/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.document.impl.tika;

import org.apache.tika.config.TikaConfig;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.document.DocumentReader;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.document.HandlerNotFoundException;
import org.exoplatform.services.document.impl.BaseDocumentReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: TikaDocumentReaderServiceImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class TikaDocumentReaderServiceImpl implements DocumentReaderService
{
   public static final String TIKA_CONFIG = "tika-config";

   public static final String TIKA_CONFIG_PATH = "tika-configuration-path";

   /**
    * User defined readers. Used to support previously created users DocumentReaders.
    */
   private final Map<String, BaseDocumentReader> userReaders;

   /**
    * Tika configuration - configured from tika-conf.xml, otherwise default used.
    */
   private final TikaConfig conf;

   public TikaDocumentReaderServiceImpl(InitParams params) throws Exception
   {
      userReaders = new HashMap<String, BaseDocumentReader>();

      // get tika configuration
      PropertiesParam param = params.getPropertiesParam(TIKA_CONFIG);
      if (param != null && param.getProperty(TIKA_CONFIG_PATH) != null)
      {
         InputStream stream =
            TikaDocumentReaderServiceImpl.class.getResourceAsStream(param.getProperty(TIKA_CONFIG_PATH));
         conf = new TikaConfig(stream);
      }
      else
      {
         conf = TikaConfig.getDefaultConfig();
      }
   }

   @Deprecated
   public String getContentAsText(String mimeType, InputStream is) throws Exception
   {
      DocumentReader reader = getDocumentReader(mimeType);
      if (reader != null)
         return reader.getContentAsText(is);
      throw new Exception("Cannot handle the document type: " + mimeType);
   }

   /**
    * This plugin registers and redefines default document reader with new one.
    * 
    * @param plugin
    */
   public void addDocumentReader(ComponentPlugin plugin)
   {
      BaseDocumentReader reader = (BaseDocumentReader)plugin;
      for (String mimeType : reader.getMimeTypes())
         userReaders.put(mimeType.toLowerCase(), reader);
   }

   /**
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.document.DocumentReaderService#getDocumentReader
    * (java.lang.String)
    */
   public DocumentReader getDocumentReader(String mimeType) throws HandlerNotFoundException
   {
      BaseDocumentReader reader = userReaders.get(mimeType.toLowerCase());
      if (reader != null)
         return reader;
      else
      {
         if (conf.getParsers().containsKey(mimeType))
         {
            return new TikaDocumentReader(conf, mimeType);
         }
         else
         {
            throw new HandlerNotFoundException("No appropriate properties extractor for " + mimeType);
         }
      }
   }

}
