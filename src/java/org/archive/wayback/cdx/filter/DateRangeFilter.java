/* DateRangeFilter
 *
 * $Id$
 *
 * Created on 1:25:49 PM Jan 24, 2006.
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
 * RecordFilter that excludes records outside of start and end range.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DateRangeFilter implements RecordFilter {

	private String first = null;
	private String last = null;
	
	/**
	 * @param first String earliest date to include
	 * @param last String latest date to include
	 */
	public DateRangeFilter(final String first, final String last) {
		this.first = first;
		this.last = last;
	}
	
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterRecord(CDXRecord record) {
		return ((first.compareTo(record.captureDate) > 0) ||
				(last.compareTo(record.captureDate) < 0)) ? 
						RECORD_EXCLUDE : RECORD_INCLUDE;
	}
}
