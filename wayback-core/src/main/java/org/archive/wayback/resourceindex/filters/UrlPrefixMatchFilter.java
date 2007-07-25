/* UrlPrefixMatchFilter
 *
 * $Id$
 *
 * Created on 3:50:12 PM Aug 17, 2006.
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
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFilter which includes any URL which begins with a given prefix, 
 * and aborts processing when any URL does not match the prefix. This abort
 * short-circuiting assumes that records will be seen in increasing URL order:
 * once a URL does not match, no further URLs will match either. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlPrefixMatchFilter implements ObjectFilter<SearchResult> {

	private String prefix;
	
	/**
	 * @param prefix String which records must begin with
	 */
	public UrlPrefixMatchFilter(final String prefix) {
		this.prefix = prefix;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(SearchResult r) {
		String resultUrl = r.get(WaybackConstants.RESULT_URL_KEY);
		return resultUrl.startsWith(prefix)	? FILTER_INCLUDE : FILTER_ABORT;
	}
}
