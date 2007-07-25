/* ObjectFilter
 *
 * $Id$
 *
 * Created on 3:10:51 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util;

/**
 * Interface for including, excluding, or aborting filtering
 *
 * @author brad
 * @version $Date$, $Revision$
 * @param <E> 
 */
public interface ObjectFilter<E> {
	/**
	 * constant indicating record should be included in the result set
	 */
	public static int FILTER_INCLUDE = 0;
	
	/**
	 * constant indicating record should be omitted from the result set
	 */
	public static int FILTER_EXCLUDE = 1;
	
	/**
	 * constant indicating record should be ommitted, and no more records should
	 * be processed.
	 */
	public static int FILTER_ABORT = 2;
	
	/** inpect record and determine if it should be included in the 
	 * results or not, or if processing of new records should stop.
	 * @param o Object which should be checked for inclusion/exclusion or abort
	 * @return int of FILTER_INCLUDE, FILTER_EXCLUDE, or FILTER_ABORT
	 */
	public int filterObject(E o);

}
