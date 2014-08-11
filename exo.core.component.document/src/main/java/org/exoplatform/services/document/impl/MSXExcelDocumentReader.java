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
package org.exoplatform.services.document.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.document.DocumentReadException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Stream based MS Excel Document Reader with low memory and cpu needs.
 */
public class MSXExcelDocumentReader extends BaseDocumentReader
{

   private static final Log LOG = ExoLogger.getLogger("exo.core.component.document.MSXExcelDocumentReader");

   private static final int MAX_TABS = 5;

   private static final int MAX_CELLTAB = 1000;

   /**
    * @see org.exoplatform.services.document.DocumentReader#getMimeTypes()
    */
   public String[] getMimeTypes()
   {
      //Supported mimetypes:
      // "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" - "x.xlsx"
      //
      //Unsupported mimetypes:
      // "application/vnd.ms-excel.sheet.binary.macroenabled.12" - "*.xlsb"; There is exceptions at parsing
      // "application/vnd.openxmlformats-officedocument.spreadsheetml.template" - "x.xltx"; Not tested
      // "application/vnd.ms-excel.sheet.macroenabled.12" - "x.xlsm"; Not tested
      // "application/vnd.ms-excel.template.macroenabled.12" - "x.xltm"; Not tested
      // "application/vnd.ms-excel.addin.macroenabled.12" - "x.xlam"; Not tested
      return new String[]{"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
   }

   public void processSheet(
       MSXExcelSheetXMLHandler.SheetContentsHandler sheetContentsExtractor,
       ReadOnlySharedStringsTable strings,
       InputStream sheetInputStream)
       throws IOException, SAXException
   {
      InputSource sheetSource = new InputSource(sheetInputStream);
      SAXParserFactory saxFactory = SAXParserFactory.newInstance();
      try {
         SAXParser saxParser = saxFactory.newSAXParser();
         XMLReader sheetParser = saxParser.getXMLReader();
         ContentHandler handler = new MSXExcelSheetXMLHandler(strings, sheetContentsExtractor, MAX_CELLTAB);
         sheetParser.setContentHandler(handler);
         sheetParser.parse(sheetSource);
      } catch (ParserConfigurationException e) {
         throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
      } catch (MSXExcelSheetXMLHandler.StopSheetParsingException e) {
         // this exception allow us to stop the parsing of the sheet when we have reached the number of cell to parse per sheet ({@link MAX_CELLTAB }
         if (LOG.isTraceEnabled()) {
            LOG.trace(e.getLocalizedMessage());
         }
      }
   }

   /**
    * Returns only a text from .xls file content with the following rules:
    * <p/>
    * we only index :
    * <ul>
    * <li>a maximum of 5000 cells per spreadsheet</li>
    * <li>a maximum of 1000 cells per tab</li>
    * <li>a maximum of 5 tabs per spreadsheet</li>
    * </ul>
    * <p/>
    * we KEEP only the following data :
    * <li> tab name</li>
    * <li> cells with string with a length > 2 chars (Strings which are not the result of a formula)</li>
    * </ul>
    * we SKIP the following data :
    * <ul>
    * <li> cells with number (date formatted or simple number)</li>
    * <li> cells with blank value</li>
    * <li> cells with boolean or error value</li>
    * <li> cells with formula</li>
    * </ul>
    *
    * @param is an input stream with .xlsx file content.
    * @return The string only with text from file content.
    */
   public String getContentAsText(final InputStream is) throws IOException, DocumentReadException
   {
      if (is == null)
      {
         throw new IllegalArgumentException("InputStream is null.");
      }
      final StringBuilder builder = new StringBuilder("");
      try
      {
         if (is.available() == 0)
         {
            return "";
         }
         try
         {
            OPCPackage container = OPCPackage.open(is);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(container);
            XSSFReader xssfReader = new XSSFReader(container);
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            MSXExcelSheetXMLHandler.SheetContentsHandler sheetExtractor = new SheetTextExtractor(builder);
            int parsedTabs = 0;
            while (iter.hasNext() && parsedTabs < MAX_TABS)
            {
               InputStream stream = null;
               parsedTabs++;
               try
               {
                  stream = iter.next();
                  builder.append('\n');
                  builder.append(iter.getSheetName());
                  builder.append('\n');
                  processSheet(sheetExtractor, strings, stream);
               }
               finally
               {
                  if (stream != null)
                  {
                     try
                     {
                        stream.close();
                     }
                     catch (IOException e)
                     {
                        if (LOG.isTraceEnabled())
                        {
                           LOG.trace("An exception occurred: " + e.getMessage());
                        }
                     }
                  }
               }
            }
         }
         catch (InvalidFormatException e)
         {
            throw new DocumentReadException("The format of the document to read is invalid.", e);
         }
         catch (SAXException e)
         {
            throw new DocumentReadException("Problem during the document parsing.", e);
         }
         catch (OpenXML4JException e)
         {
            throw new DocumentReadException("Problem during the document parsing.", e);
         }
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException e)
            {
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("An exception occurred: " + e.getMessage());
               }
            }
         }
      }
      return builder.toString();
   }

   public String getContentAsText(InputStream is, String encoding) throws IOException, DocumentReadException
   {
      // Ignore encoding
      return getContentAsText(is);
   }

   protected class SheetTextExtractor implements MSXExcelSheetXMLHandler.SheetContentsHandler
   {
      private final StringBuilder output;

      private boolean firstCellOfRow = true;

      protected SheetTextExtractor(StringBuilder output)
      {
         this.output = output;
      }

      public void startRow(int rowNum)
      {
         firstCellOfRow = true;
      }

      public void endRow()
      {
         output.append('\n');
      }

      public void cell(String cellRef, String formattedValue)
      {
         if (firstCellOfRow)
         {
            firstCellOfRow = false;
         }
         else
         {
            if (formattedValue != null && formattedValue.length() > 2)
            {
               output.append(' ');
            }
         }
         if (formattedValue != null && formattedValue.length() > 2)
         {
            output.append(formattedValue);
         }
      }

      public void headerFooter(String text, boolean isHeader, String tagName)
      {
         // We don't include headers in the output yet, so ignore
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.exoplatform.services.document.DocumentReader#getProperties(java.io.
    *      InputStream)
    */
   public Properties getProperties(final InputStream is) throws IOException, DocumentReadException
   {
         POIPropertiesReader reader = new POIPropertiesReader();
      reader.readDCProperties(SecurityHelper
         .doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<XSSFWorkbook>()
         {
            public XSSFWorkbook run() throws Exception
            {
               return new XSSFWorkbook(is);
            }
         }));

         return reader.getProperties();
      }
   }
