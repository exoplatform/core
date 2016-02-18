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
package org.exoplatform.services.document.diff;

/**
 * A simple interface for implementations of differencing algorithms.
 */
public interface DiffAlgorithm
{
   /**
    * Computes the difference between the original sequence and the revised
    * sequence and returns it as a
    * {@link org.exoplatform.services.document.impl.diff.RevisionImpl Revision} object.
    * <p>
    * The revision can be used to construct the revised sequence from the
    * original sequence.
    * 
    * @param rev the revised text
    * @return the revision script.
    * @throws Exception if the diff could not be computed.
    */
   public Revision diff(Object[] orig, Object[] rev) throws Exception;
}
