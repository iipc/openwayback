/* LiveWebLocalResourceIndex
 *
 * $Id$
 *
 * Created on 5:53:29 PM Mar 13, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.liveweb;

import java.util.ArrayList;

import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.bdb.BDBIndex;
import org.archive.wayback.resourceindex.bdb.SearchResultToBDBRecordAdapter;
import org.archive.wayback.util.AdaptedIterator;

/**
 * Alternate LocalResourceIndex that supports an alternate BDB configuration,
 * and allows adding of SearchResults to the index.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LiveWebLocalResourceIndex extends LocalResourceIndex {

	/**
	 * Add a single SearchResult to the index.
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	public void addSearchResult(SearchResult result) {
		ArrayList<SearchResult> l = new ArrayList<SearchResult>();
		l.add(result);
		BDBIndex bdbSource = (BDBIndex) source;
		bdbSource.insertRecords(new AdaptedIterator(l.iterator(),
				new SearchResultToBDBRecordAdapter()));
	}
}
