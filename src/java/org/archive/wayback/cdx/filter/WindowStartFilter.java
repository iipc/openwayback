/* WindowStartFilter
 *
 * $Id$
 *
 * Created on 1:11:21 PM Jan 24, 2006.
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
 * RecordFitler that omits the first N records seen.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WindowStartFilter implements RecordFilter {

	private int windowStart = 0;
	private int numSeen = 0;
	
	/**
	 * @param windowStart int number of records to skip before including any
	 */
	public WindowStartFilter(int windowStart) {
		this.windowStart = windowStart;
		this.numSeen = 0;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterRecord(CDXRecord record) {
		numSeen++;
		if(numSeen > windowStart) {
			return RECORD_INCLUDE;
		}
		return RECORD_EXCLUDE;
	}

}
