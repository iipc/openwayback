/* RecordFilter
 *
 * $Id$
 *
 * Created on 12:37:32 PM Jan 24, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.cdx.filter;

import org.archive.wayback.cdx.CDXRecord;

/**
 * RecordFilter is the interface for chaining together logic on record 
 * inclusion/exclusion for wayback Search Results.
 *
 * Assumed that chain logic will be AND (all filters must include for inclusion)
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface RecordFilter {

	/**
	 * constant indicating record should be included in the result set
	 */
	public static int RECORD_INCLUDE = 0;
	
	/**
	 * constant indicating record should be omitted from the result set
	 */
	public static int RECORD_EXCLUDE = 1;
	
	/**
	 * constant indicating record should be ommitted, and no more records should
	 * be processed.
	 */
	public static int RECORD_ABORT = 2;
	
	/** inpect record and determine if it should be included in the 
	 * results or not, or if processing of new records should stop.
	 * @param record which should be checked for inclusion/exclusion or abort
	 * @return int of RECORD_INCLUDE, RECORD_EXCLUDE, or RECORD_ABORT
	 */
	public int filterRecord(CDXRecord record);

}
