/* HostMatchFilter
 *
 * $Id$
 *
 * Created on 3:36:51 PM Aug 17, 2006.
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

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.SearchResultFilter;

/**
 * SearchResultFilter which includes only records that have original host 
 * matching.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class HostMatchFilter extends SearchResultFilter {

	private String hostname = null;
	
	/**
	 * @param hostname String of original host to match
	 */
	public HostMatchFilter(final String hostname) {
		this.hostname = hostname;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterSearchResult(SearchResult r) {
		String origHost = r.get(WaybackConstants.RESULT_ORIG_HOST);
		return hostname.equals(origHost) ? FILTER_INCLUDE : FILTER_EXCLUDE;
	}
}
