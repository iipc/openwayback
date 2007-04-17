/* PathQuerySearchResultPartitioner
 *
 * $Id$
 *
 * Created on 4:09:51 PM Apr 16, 2007.
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
package org.archive.wayback.query;

import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;

/**
 * UI Utility object that transforms a SearchResults into a series of 
 * PathQuerySearchResultPartition objects, one object per URL in the 
 * SearchResults. This makes rendering of summary pages for a series of urls,
 * with potentially multiple captures of each URL simpler.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class PathQuerySearchResultPartitioner {
	private ArrayList<PathQuerySearchResultPartition> partitions = null;
	private int totalResultCount = 0;
	/**
	 * Constructor
	 * @param results
	 * @param uriConverter 
	 */
	public PathQuerySearchResultPartitioner(SearchResults results, 
			ResultURIConverter uriConverter) {
		partitions = new ArrayList<PathQuerySearchResultPartition>();
		Iterator<SearchResult> itr = results.iterator();
		PathQuerySearchResultPartition current = null;
		totalResultCount = 0;
		while(itr.hasNext()) {
			totalResultCount++;
			SearchResult result = itr.next();
			if((current == null) || !current.sameUrl(result)) {
				if(current != null) {
					partitions.add(current);
				}
				current = new PathQuerySearchResultPartition(result,
						uriConverter);
			} else {
				current.addSearchResult(result);
			}
		}
		if(current != null) {
			partitions.add(current);
		}
	}

	/**
	 * @return the total number of unique urls found in the SearchResults
	 */
	public int numUrls() {
		return partitions.size();
	}

	/**
	 * @return the total number of captures for all urls in the SearchResults
	 */
	public int numResultsTotal() {
		return totalResultCount;
	}
	
	/**
	 * @return an Iterator of PathQuerySearchResultPartition objects
	 */
	public Iterator<PathQuerySearchResultPartition> iterator() {
		return partitions.iterator();
	}
}
