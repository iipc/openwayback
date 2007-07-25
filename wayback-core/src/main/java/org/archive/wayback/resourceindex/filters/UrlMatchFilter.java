/* UrlMatchFilter
 *
 * $Id$
 *
 * Created on 3:47:58 PM Aug 17, 2006.
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
 * SearchResultFilter which includes only records that have url matching
 * aborts as soon as url does not match.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlMatchFilter implements ObjectFilter<SearchResult> {

	private String url = null;
	
	/**
	 * @param url String of url to match
	 */
	public UrlMatchFilter(final String url) {
		this.url = url;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(SearchResult r) {
		String resultUrl = r.get(WaybackConstants.RESULT_URL_KEY);
		return url.equals(resultUrl) ? 
				FILTER_INCLUDE : FILTER_ABORT;
	}
}
