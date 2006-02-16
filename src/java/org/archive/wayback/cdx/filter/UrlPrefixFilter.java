/* UrlPrefixFilter
 *
 * $Id$
 *
 * Created on 2:56:43 PM Jan 24, 2006.
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
 * RecordFilter which includes any URL which begins with a given prefix, and
 * aborts processing when any URL does not match the prefix. This abort
 * short-circuiting assumes that records will be seen in increasing URL order:
 * once a URL does not match, no further URLs will match either. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlPrefixFilter implements RecordFilter {

	private String prefix;
	
	/**
	 * @param prefix String which records must begin with
	 */
	public UrlPrefixFilter(final String prefix) {
		this.prefix = prefix;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterRecord(CDXRecord record) {
		return record.url.startsWith(prefix) ? RECORD_INCLUDE : RECORD_ABORT;
	}

}
