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

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlSearchResults extends SearchResults {
	/**
	 * List of UrlSearchResult objects for index records matching a query
	 */
	private ArrayList<UrlSearchResult> results = 
		new ArrayList<UrlSearchResult>();

	public void addSearchResult(UrlSearchResult result) {
		addSearchResult(result,true);
	}

	public void addSearchResult(UrlSearchResult result, boolean append) {
		if(append) {
			results.add(result);
		} else {
			results.add(0,result);
		}
	}

	public boolean isEmpty() {
		return results.isEmpty();
	}

	public Iterator<UrlSearchResult> iterator() {
		return results.iterator();
	}

	public int size() {
		return results.size();
	}
}
