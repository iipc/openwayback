/* MimeTypeFilter
 *
 * $Id$
 *
 * Created on 3:41:41 PM Aug 17, 2006.
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
package org.archive.wayback.resourceindex.filters;

import java.util.HashMap;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.SearchResultFilter;

/**
 * SearchResultFilter which includes only records matching one or more supplied
 * Mime-Types. All comparision is case-insensitive.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class MimeTypeFilter extends SearchResultFilter {
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
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterSearchResult(SearchResult r) {
		String mime = r.get(WaybackConstants.RESULT_MIME_TYPE).toLowerCase();
		return validMimes.containsKey(mime) ? FILTER_INCLUDE : FILTER_EXCLUDE;
	}
}
