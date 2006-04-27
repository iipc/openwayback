/* StartDateFilter
 *
 * $Id$
 *
 * Created on 5:09:01 PM Apr 13, 2006.
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
 * RecordFilter which includes all records until 1 is found before start date
 * then it aborts processing. Assumed usage is for URL matches, when records
 * will be ordered by capture date and traversed in REVERSE ORDER, in which
 * case the first record before the startDate provided indicates that no 
 * further records will possibly match.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StartDateFilter implements RecordFilter {

	private String startDate = null;
	
	/**
	 * @param startDate String timestamp which marks the end of includable records
	 */
	public StartDateFilter(final String startDate) {
		this.startDate = startDate;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterRecord(CDXRecord record) {
		return (startDate.substring(0,record.captureDate.length()).compareTo(
				record.captureDate) > 0) ? 
				RECORD_ABORT : RECORD_INCLUDE; 
	}

}
