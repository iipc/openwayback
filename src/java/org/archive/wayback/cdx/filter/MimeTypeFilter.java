/* MimeTypeFilter
 *
 * $Id$
 *
 * Created on 1:51:52 PM Jan 24, 2006.
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

import java.util.HashMap;

import org.archive.wayback.cdx.CDXRecord;

/**
 * RecordFilter which includes only records matching one or more supplied
 * Mime-Types. All comparision is case-insensitive.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class MimeTypeFilter implements RecordFilter {

	private HashMap validMimes = null;
	
	/**
	 * @param mime String which is valid match for mime-type field
	 */
	public void addMime(final String mime) {
		if(validMimes == null) {
			validMimes = new HashMap();
		}
		validMimes.put(mime.toLowerCase(),new Integer(1));
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterRecord(CDXRecord record) {
		String mime = record.mimeType.toLowerCase();
		return validMimes.containsKey(mime) ? RECORD_INCLUDE : RECORD_EXCLUDE;
	}
}
