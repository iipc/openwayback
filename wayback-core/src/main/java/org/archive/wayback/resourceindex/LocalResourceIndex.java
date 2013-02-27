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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.URIException;
import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.UrlSearchResult;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.RuntimeIOException;
import org.archive.wayback.resourceindex.adapters.CaptureToUrlSearchResultIterator;
import org.archive.wayback.resourceindex.filterfactory.AccessPointCaptureFilterGroupFactory;
import org.archive.wayback.resourceindex.filterfactory.AnnotatingCaptureFilterGroupFactory;
import org.archive.wayback.resourceindex.filterfactory.CaptureFilterGroup;
import org.archive.wayback.resourceindex.filterfactory.ClosestTrackingCaptureFilterGroupFactory;
import org.archive.wayback.resourceindex.filterfactory.CoreCaptureFilterGroupFactory;
import org.archive.wayback.resourceindex.filterfactory.ExclusionCaptureFilterGroupFactory;
import org.archive.wayback.resourceindex.filterfactory.FilterGroupFactory;
import org.archive.wayback.resourceindex.filterfactory.QueryCaptureFilterGroupFactory;
import org.archive.wayback.resourceindex.filterfactory.WindowFilterGroup;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;
import org.archive.wayback.util.ObjectFilterIterator;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;
import org.archive.wayback.webapp.PerfStats;

/**
 * ResourceIndex implementation which assumes a "local" SearchResultSource.
 * 
 * Extracting SearchResults from the source involves several layered steps:
 * 
 * 1) extraction of results based on a prefix into the index
 * 2) passing each result through a series of adapters
 *       these adapters can create new fields based on existing fields, or can
 *       annotate fields as they are scanned in order
 * 3) filtering results based on request filters, which may come from 
 *       * WaybackRequest-specific parameters. 
 *           Ex. exact host match only, exact scheme match only, ...
 *       * AccessPoint-specific configuration 
 *           Ex. only return records with (ARC/WARC) filename prefixed with XXX
 *           Ex. block any dates not older than 6 months
 * 4) filtering based on AccessControl configurations
 *        Ex. block any urls with prefixes in file X
 * 5) windowing filters, which provide pagination of the results, allowing
 *        requests to specify "show results between 10 and 20"
 * 6) post filter adapters, which may annotate final results with other 
 *        information
 *        Ex. for each result, consult DB to see if user-contributed messages
 *            apply to the results 
 * 
 * After all results have been processed, we annotate the final SearchResultS
 * object with summary information about the results included. As we set up the 
 * chain of filters, we instrument the chain with counters that observe the 
 * number of results that went into, and came out of the Exclusion filters.
 * 
 * If there were results presented to the Exclusion filter, but none were 
 * emitted from it, an AccessControlException is thrown.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LocalResourceIndex implements ResourceIndex {
	public final static int TYPE_REPLAY = 0;
	public final static int TYPE_CAPTURE = 1;
	public final static int TYPE_URL = 2;

	/**
	 * maximum number of records to return
	 */
	private final static int MAX_RECORDS = 1000;
	
	enum PerfStat
	{
		IndexLoad;
	}
	
	private int maxRecords = MAX_RECORDS;

	protected SearchResultSource source;
	
	private UrlCanonicalizer canonicalizer = null;
	
	private boolean dedupeRecords = false;
	
	private boolean timestampSearch = false;
	
	private boolean markPrefixQueries = false;
	
	private ObjectFilter<CaptureSearchResult> annotater = null;
	
	private ObjectFilter<CaptureSearchResult> filter = null;

	
	protected List<FilterGroupFactory> fgFactories = null;
	
	public LocalResourceIndex() {
		canonicalizer = new AggressiveUrlCanonicalizer();
		fgFactories = new ArrayList<FilterGroupFactory>();
		fgFactories.add(new CoreCaptureFilterGroupFactory());		
		fgFactories.add(new QueryCaptureFilterGroupFactory());		
		fgFactories.add(new AccessPointCaptureFilterGroupFactory());
		fgFactories.add(new AnnotatingCaptureFilterGroupFactory());
		fgFactories.add(new ExclusionCaptureFilterGroupFactory());
		fgFactories.add(new ClosestTrackingCaptureFilterGroupFactory());
	}

	private void cleanupIterator(CloseableIterator<? extends SearchResult> itr)
	throws ResourceIndexNotAvailableException {
		try {
			itr.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException(
					e.getLocalizedMessage());
		}
	}
	
	protected List<CaptureFilterGroup> getRequestFilterGroups(WaybackRequest r) 
	throws BadQueryException {
		
		ArrayList<CaptureFilterGroup> groups = 
			new ArrayList<CaptureFilterGroup>();
		for(FilterGroupFactory f : fgFactories) {
			groups.add(f.getGroup(r, canonicalizer, this));
		}
		return groups;
	}
	
	
	public CaptureSearchResults doCaptureQuery(WaybackRequest wbRequest,
			int type) throws ResourceIndexNotAvailableException,
		ResourceNotInArchiveException, BadQueryException,
		AccessControlException {

		String urlKey;
		try {
			urlKey = canonicalizer.urlStringToKey(wbRequest.getRequestUrl());
		} catch (IOException e) {
			throw new BadQueryException("Bad URL(" + 
					wbRequest.getRequestUrl() + ")");
		}
		
		// Special handling for index where the key is url<space>timestamp
		// for faster binary search lookup
		if (timestampSearch && wbRequest.isTimestampSearchKey()) {
			String replayTimestamp = wbRequest.getReplayTimestamp();
			
			if (replayTimestamp != null) {	
				urlKey += " " + replayTimestamp;
			}
		}

		// the CaptureSearchResults we are about to return:
		CaptureSearchResults results = new CaptureSearchResults();
		// the various filters to apply to the results:
		ObjectFilterChain<CaptureSearchResult> filters = 
			new ObjectFilterChain<CaptureSearchResult>();

		// Groupings of filters for... sanity and summary annotation of results:
		// Windows:
		WindowFilterGroup<CaptureSearchResult> window = 
			new WindowFilterGroup<CaptureSearchResult>(wbRequest,this);
		List<CaptureFilterGroup> groups = getRequestFilterGroups(wbRequest); 
		if(filter != null) {
			filters.addFilter(filter);
		}

		for(CaptureFilterGroup cfg : groups) {
			filters.addFilters(cfg.getFilters());
		}
		filters.addFilters(window.getFilters());
		
		CloseableIterator<CaptureSearchResult> itr = null;
		
		try {
			PerfStats.timeStart(PerfStat.IndexLoad);
			
			itr = new ObjectFilterIterator<CaptureSearchResult>(source.getPrefixIterator(urlKey),filters);
			
			while(itr.hasNext()) {
				results.addSearchResult(itr.next());
			}
		} catch(RuntimeIOException e) {
			throw new ResourceIndexNotAvailableException(e.getLocalizedMessage());
		} finally {
			if (itr != null) {
				cleanupIterator(itr);
			}
			
			PerfStats.timeEnd(PerfStat.IndexLoad);
		}
		
		for(CaptureFilterGroup cfg : groups) {
			cfg.annotateResults(results);
		}
		
		window.annotateResults(results);

		return results;
	}

	public UrlSearchResults doUrlQuery(WaybackRequest wbRequest)
		throws ResourceIndexNotAvailableException, 
		ResourceNotInArchiveException, BadQueryException, 
		AccessControlException {
		
		String urlKey;
		try {
			urlKey = canonicalizer.urlStringToKey(wbRequest.getRequestUrl());
		} catch (URIException e) {
			throw new BadQueryException("Bad URL(" + 
					wbRequest.getRequestUrl() + ")");
		}
		
		if (markPrefixQueries) {
			urlKey += "*\t";
		}

		UrlSearchResults results = new UrlSearchResults();

		// the various CAPTURE filters to apply to the results:
		ObjectFilterChain<CaptureSearchResult> cFilters = 
			new ObjectFilterChain<CaptureSearchResult>();

		
		// Groupings of filters for clarity(?) and summary annotation of 
		// results:
		List<CaptureFilterGroup> groups = getRequestFilterGroups(wbRequest); 
		for(CaptureFilterGroup cfg : groups) {
			cFilters.addFilters(cfg.getFilters());
		}
		if (filter != null) {
			cFilters.addFilter(filter);
		}
		

		// we've filtered the appropriate CaptureResult objects within the 
		// iterator, now we're going to convert whatever records make it past
		// the filters into UrlSearchResults, and then do further window
		// filtering on those results:
		// Windows:
		// the window URL filters to apply to the results, once they're 
		// UrlSearchResult objects
		ObjectFilterChain<UrlSearchResult> uFilters = 
			new ObjectFilterChain<UrlSearchResult>();
		WindowFilterGroup<UrlSearchResult> window = 
			new WindowFilterGroup<UrlSearchResult>(wbRequest,this);
		uFilters.addFilters(window.getFilters());

		CloseableIterator<CaptureSearchResult> itrC = null;
		CloseableIterator<UrlSearchResult> itrU = null;
		
		try {
			PerfStats.timeStart(PerfStat.IndexLoad);
			
			itrC = new ObjectFilterIterator<CaptureSearchResult>(
					source.getPrefixIterator(urlKey),cFilters);	
		
			itrU = new ObjectFilterIterator<UrlSearchResult>(
						new CaptureToUrlSearchResultIterator(itrC),
						uFilters);
		
			while(itrU.hasNext()) {
				results.addSearchResult(itrU.next());
			}
		} finally {
			if (itrU != null) {
				cleanupIterator(itrU);
			}
			PerfStats.timeEnd(PerfStat.IndexLoad);
		}
		
		for(CaptureFilterGroup cfg : groups) {
			cfg.annotateResults(results);
		}
		window.annotateResults(results);

		return results;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.ResourceIndex#query(org.archive.wayback.core.WaybackRequest)
	 */
	public SearchResults query(WaybackRequest wbRequest)
			throws ResourceIndexNotAvailableException,
			ResourceNotInArchiveException, BadQueryException,
			AccessControlException {
		SearchResults results = null; // return value placeholder

		if (wbRequest.isReplayRequest()) {

			results = doCaptureQuery(wbRequest, TYPE_REPLAY);
			results.putFilter(WaybackRequest.REQUEST_TYPE, 
					WaybackRequest.REQUEST_REPLAY_QUERY);

		} else if (wbRequest.isCaptureQueryRequest()) {

			results = doCaptureQuery(wbRequest, TYPE_CAPTURE);
			results.putFilter(WaybackRequest.REQUEST_TYPE, 
					WaybackRequest.REQUEST_CAPTURE_QUERY);

		} else if (wbRequest.isUrlQueryRequest()) {

			results = doUrlQuery(wbRequest);
			results.putFilter(WaybackRequest.REQUEST_TYPE, 
					WaybackRequest.REQUEST_URL_QUERY);

		} else {

			throw new BadQueryException("Unknown query type, must be " 
					+ WaybackRequest.REQUEST_REPLAY_QUERY
					+ ", " + WaybackRequest.REQUEST_CAPTURE_QUERY 
					+ ", or " + WaybackRequest.REQUEST_URL_QUERY);
		}
		return results;
	}

	public void addSearchResults(Iterator<CaptureSearchResult> itr) throws IOException,
		UnsupportedOperationException {
		if(source instanceof UpdatableSearchResultSource) {
			UpdatableSearchResultSource updatable = 
				(UpdatableSearchResultSource) source;
			updatable.addSearchResults(itr,canonicalizer);
		} else {
			throw new UnsupportedOperationException("Underlying " +
					"SearchResultSource is not Updatable.");
		}
	}

	public boolean isUpdatable() {
		return (source instanceof UpdatableSearchResultSource);
	}
	
	/**
	 * @param maxRecords the maxRecords to set
	 */
	public void setMaxRecords(int maxRecords) {
		this.maxRecords = maxRecords;
	}
	public int getMaxRecords() {
		return maxRecords;
	}


	/**
	 * @param source the source to set
	 */
	public void setSource(SearchResultSource source) {
		this.source = source;
	}

	public boolean isDedupeRecords() {
		return dedupeRecords;
	}

	public void setDedupeRecords(boolean dedupeRecords) {
		this.dedupeRecords = dedupeRecords;
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	public void shutdown() throws IOException {
		source.shutdown();
	}

	public ObjectFilter<CaptureSearchResult> getAnnotater() {
		return annotater;
	}

	public void setAnnotater(ObjectFilter<CaptureSearchResult> annotater) {
		this.annotater = annotater;
	}

	public ObjectFilter<CaptureSearchResult> getFilter() {
		return filter;
	}

	public void setFilter(ObjectFilter<CaptureSearchResult> filter) {
		this.filter = filter;
	}

	public boolean isTimestampSearch() {
		return timestampSearch;
	}

	public void setTimestampSearch(boolean timestampSearch) {
		this.timestampSearch = timestampSearch;
	}

	public boolean isMarkPrefixQueries() {
		return markPrefixQueries;
	}

	public void setMarkPrefixQueries(boolean markPrefixQueries) {
		this.markPrefixQueries = markPrefixQueries;
	}
}
