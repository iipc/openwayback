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
package org.archive.wayback.resourceindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.cdx.CDXIndex;
import org.archive.wayback.util.CompositeSortedIterator;

/**
 * SearchResultSource that aggregates results from multiple SearchResultSources.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class CompositeSearchResultSource implements SearchResultSource {

	protected List<SearchResultSource> sources;

	/**
	 * Constructor
	 */
	public CompositeSearchResultSource() {
		sources = new ArrayList<SearchResultSource>();
	}

	/**
	 * add a SearchResultSource to this composite
	 * 
	 * @param source
	 *            to be added
	 */
	public void addSource(SearchResultSource source) {
		sources.add(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator<CaptureSearchResult> getPrefixIterator(String prefix)
			throws ResourceIndexNotAvailableException {

		Comparator<CaptureSearchResult> comparator = new SearchResultComparator();
		CompositeSortedIterator<CaptureSearchResult> itr = 
			new CompositeSortedIterator<CaptureSearchResult>(comparator);
		
		int added = 0;
		ResourceIndexNotAvailableException lastExc = null;
		
		try {
			for (int i = 0; i < sources.size(); i++) {
				itr.addComponent(sources.get(i).getPrefixIterator(prefix));
				added++;
			}
		} catch (ResourceIndexNotAvailableException e) {
			lastExc = e;
		}
		
		if ((lastExc != null) && (added == 0)) {
			try {
				itr.close();
			} catch (IOException io) {
				
			}
			throw lastExc;
		}
		
		return itr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixReverseIterator(java.lang.String)
	 */
	public CloseableIterator<CaptureSearchResult> getPrefixReverseIterator(
			String prefix) throws ResourceIndexNotAvailableException {

		Comparator<CaptureSearchResult> comparator = new SearchResultComparator(true);
		CompositeSortedIterator<CaptureSearchResult> itr = 
			new CompositeSortedIterator<CaptureSearchResult>(comparator);
		for (int i = 0; i < sources.size(); i++) {
			itr.addComponent(sources.get(i).getPrefixReverseIterator(prefix));
		}
		return itr;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#cleanup(org.archive.wayback.util.CleanableIterator)
	 */
	public void cleanup(CloseableIterator<CaptureSearchResult> c) throws IOException{
		c.close();
	}

	/**
	 * @return null -- only present for Spring
	 */
	public List<String> getCDXSources() {
		return null;
	}
	
	/**
	 * Sets the list of files searched for queries against this 
	 * SearchResultSource to the list of paths cdxs
	 * @param cdxs
	 */
	public void setCDXSources(List<String> cdxs) {
		sources = new ArrayList<SearchResultSource>();
		for(int i = 0; i < cdxs.size(); i++) {
			CDXIndex index = new CDXIndex();
			index.setPath(cdxs.get(i));
			addSource(index);
		}
	}
	
	/**
	 * @param sources the sources to set
	 */
	public void setSources(List<SearchResultSource> sources) {
		this.sources = sources;
	}

	/**
	 * @return the sources
	 */
	public List<SearchResultSource> getSources() {
		return sources;
	}

	public void shutdown() throws IOException {
		for(SearchResultSource source : sources) {
			source.shutdown();
		}
	}
}
