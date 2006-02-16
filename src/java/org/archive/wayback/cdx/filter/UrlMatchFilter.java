/* UrlMatchFilter
 *
 * $Id$
 *
 * Created on 1:06:53 PM Jan 24, 2006.
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
 * RecordFilters which includes only records that have url matching
 * aborts as soon as url does not match.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlMatchFilter implements RecordFilter {

	private String url = null;
	
	/**
	 * @param url String of url to match
	 */
	public UrlMatchFilter(final String url) {
		this.url = url;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterRecord(CDXRecord record) {
		return url.equals(record.url) ? 
				RECORD_INCLUDE : RECORD_ABORT;
	}

}
