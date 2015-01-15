/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual
 *  contributors.
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.core;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Output of URL query.
 *
 * @author brad
 */
public class UrlSearchResults extends SearchResults implements Iterable<UrlSearchResult> {
	/**
	 * List of UrlSearchResult objects for index records matching a query
	 */
	private ArrayList<UrlSearchResult> results = new ArrayList<UrlSearchResult>();

	public void addSearchResult(UrlSearchResult result) {
		addSearchResult(result, true);
	}

	public void addSearchResult(UrlSearchResult result, boolean append) {
		if (append) {
			results.add(result);
		} else {
			results.add(0, result);
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
