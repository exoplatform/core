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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.document.DocumentReadException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS A parser of Microsoft Excel files.
 * 
 * @author <a href="mailto:phunghainam@gmail.com">Phung Hai Nam</a>
 * @author Gennady Azarenkov
 * @version Oct 21, 2005
 */
public class MSExcelDocumentReader extends BaseDocumentReader
{

   private static final Log LOG = ExoLogger.getLogger("exo.core.component.document.MSExcelDocumentReader");

   private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

   private static final int MAX_TAB = 5;

   private static final int MAX_CELL = 1000;
   
   /**
    * Get the application/excel mime type.
    * 
    * @return The string with application/excel mime type.
    */
   public String[] getMimeTypes()
   {
      return new String[]{"application/excel", "application/xls", "application/vnd.ms-excel"};
   }

   /**
    * Returns only a text from .xls file content.
    * 
    * @param is an input stream with .xls file content.
    * @return The string only with text from file content.
    */
   public String getContentAsText(InputStream is) throws IOException, DocumentReadException
   {
      if (is == null)
      {
         throw new IllegalArgumentException("InputStream is null.");
      }

      final StringBuilder builder = new StringBuilder("");
      
      SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

      try
      {
         if (is.available() == 0)
         {
            return "";
         }
         
         HSSFWorkbook wb;
         try
         {
            wb = new HSSFWorkbook(is);
         }
         catch (IOException e)
         {
            throw new DocumentReadException("Can't open spreadsheet.", e);
         }
         for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets() && sheetNum < MAX_TAB; sheetNum++)
         {
            HSSFSheet sheet = wb.getSheetAt(sheetNum);
            if (sheet != null)
            {
               int countCell = MAX_CELL;
               for (int rowNum = sheet.getFirstRowNum(); rowNum <= sheet.getLastRowNum() && countCell > 0; rowNum++)
               {
                  HSSFRow row = sheet.getRow(rowNum);

                  if (row != null)
                  {
                     int lastcell = row.getLastCellNum();
                     for (int k = 0; k < lastcell && countCell > 0; k++)
                     {
                        final HSSFCell cell = row.getCell((short)k);
                        countCell --;
                        if (cell != null)
                        {
                           switch (cell.getCellType())
                           {
                              case HSSFCell.CELL_TYPE_NUMERIC : {
                                 double d = cell.getNumericCellValue();
                                 if (isCellDateFormatted(cell))
                                 {
                                    Date date = HSSFDateUtil.getJavaDate(d);
                                    String cellText = dateFormat.format(date);
                                    builder.append(cellText).append(" ");
                                 }
                                 else
                                 {
                                   builder.append(d).append(" ");
                                 }
                                 break;
                              }
                              case HSSFCell.CELL_TYPE_STRING :
                                 SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
                                 {
                                    public Void run()
                                    {
                                       builder.append(cell.getStringCellValue().toString()).append(" ");
                                       return null;
                                    }
                                 });
                                 break;
                              default :
                                 break;
                           }
                        }
                     }
                  }
               }
            }
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

   /*
    * (non-Javadoc)
    * 
    * @see org.exoplatform.services.document.DocumentReader#getProperties(java.io.
    *      InputStream)
    */
   public Properties getProperties(InputStream is) throws IOException, DocumentReadException
   {
      POIPropertiesReader reader = new POIPropertiesReader();
      reader.readDCProperties(is);
      return reader.getProperties();
   }

   public static boolean isCellDateFormatted(HSSFCell cell)
   {
      boolean bDate = false;
      double d = cell.getNumericCellValue();
      if (HSSFDateUtil.isValidExcelDate(d))
      {
         HSSFCellStyle style = cell.getCellStyle();
         int i = style.getDataFormat();
         switch (i)
         {
            case 0xe : // m/d/yy
            case 0xf : // d-mmm-yy
            case 0x10 : // d-mmm
            case 0x11 : // mmm-yy
            case 0x12 : // h:mm AM/PM
            case 0x13 : // h:mm:ss AM/PM
            case 0x14 : // h:mm
            case 0x15 : // h:mm:ss
            case 0x16 : // m/d/yy h:mm
            case 0x2d : // mm:ss
            case 0x2e : // [h]:mm:ss
            case 0x2f : // mm:ss.0

            case 0xa5 : // ??
            case 0xa7 : // ??
            case 0xa9 : // ??

            case 0xac : // mm:dd:yy not specified in javadoc
            case 0xad : // yyyy-mm-dd not specified in javadoc
            case 0xae : // mm:dd:yyyy not specified in javadoc
            case 0xaf : // m:d:yy not specified in javadoc
               bDate = true;
               break;
            default :
               bDate = false;
               break;
         }
      }
      return bDate;
   }
}
