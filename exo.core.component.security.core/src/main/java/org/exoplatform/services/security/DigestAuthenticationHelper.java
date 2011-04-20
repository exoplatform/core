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
package org.exoplatform.services.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Dmitry Kuleshov
 * @version $Id:$
 */

public class DigestAuthenticationHelper
{

   /**
    * Number of HEX digits used for A1, A2 strings and password encoding. 
    * More information is settled in<a href=http://www.apps.ietf.org/rfc/rfc2617.html#sec-3.2.2>RFC-2617</a>.
    */
   private static int HASH_HEX_LENGTH = 32;

   public static String calculatePassword(String username, String originalPassword, Map<String, String> passwordContext)
      throws NoSuchAlgorithmException
   {
      // fetch needed data
      String nc = passwordContext.get("nc");
      String a2 = passwordContext.get("md5a2");
      String uri = passwordContext.get("uri");
      String qop = passwordContext.get("qop");
      String nonce = passwordContext.get("nonce");
      String realm = passwordContext.get("realmName");
      String cnonce = passwordContext.get("cnonce");
      String entity = passwordContext.get("entity");
      String method = passwordContext.get("method");
      if (realm == null)
      {
         // in case we have a jboss server, it uses 'realm' name
         realm = passwordContext.get("realm");
      }
      if (a2 == null)
      {
         // in case we have a jboss server, it uses 'a2hash' name
         a2 = passwordContext.get("a2hash");
      }

      MessageDigest md = MessageDigest.getInstance("MD5");
      // calculate MD5 hash of A1 string
      String a1 = username + ":" + realm + ":" + originalPassword;
      md.update(a1.getBytes());
      // encode A1 in HEX digits
      a1 = convertToHex(md.digest());

      // if encoded A2 MD5 hash is not supplied by server
      // we need to calculate it manually
      if (a2 == null)
      {
         if (qop.equals("auth"))
         {
            md.update((method + ":" + uri).getBytes());
            a2 = convertToHex(md.digest());
         }
         else if (qop.equals("auth-int"))
         {
            md.update((method + ":" + uri + ":" + convertToHex(entity.getBytes())).getBytes());
            a2 = convertToHex(md.digest());
         }
      }

      // create a digest using provided data
      String digest = a1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + a2;
      md.update(digest.getBytes());
      // return encoded hash using HEX digits digest
      return convertToHex(md.digest());
   }

   public static String convertToHex(byte[] bin)
   {
      StringBuffer tmpStr = new StringBuffer(HASH_HEX_LENGTH);
      int digit;

      for (int i = 0; i < HASH_HEX_LENGTH / 2; i++)
      {
         // get integer presentation of left 4 bits of byte
         digit = (bin[i] >> 4) & 0xf;
         // append HEX digit 
         tmpStr.append(Integer.toHexString(digit));
         // get integer presentation of right 4 bits of byte
         digit = bin[i] & 0xf;
         tmpStr.append(Integer.toHexString(digit));

      };
      return tmpStr.toString();
   }
}
