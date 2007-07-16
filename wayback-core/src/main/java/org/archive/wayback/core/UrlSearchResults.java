/* UrlSearchResults
 *
 * $Id$
 *
 * Created on 4:06:03 PM Apr 19, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.core;

import org.archive.wayback.WaybackConstants;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlSearchResults extends SearchResults {

	public String getResultsType() {
		return WaybackConstants.RESULTS_TYPE_URL;
	}
	public void addSearchResult(SearchResult result) {
		addSearchResult(result,true);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.core.SearchResults#addSearchResult(org.archive.wayback.core.SearchResult, boolean)
	 */
	@Override
	public void addSearchResult(SearchResult result, boolean append) {
		addSearchResultRaw(result,append);
	}
}
