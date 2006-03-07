/* LocalBDBResourceIndex
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.cdx;

import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.cdx.filter.CounterFilter;
import org.archive.wayback.cdx.filter.DateRangeFilter;
import org.archive.wayback.cdx.filter.EndDateFilter;
import org.archive.wayback.cdx.filter.ExclusionFilter;
import org.archive.wayback.cdx.filter.FilterChain;
import org.archive.wayback.cdx.filter.GuardRailFilter;
import org.archive.wayback.cdx.filter.RecordFilter;
import org.archive.wayback.cdx.filter.UrlMatchFilter;
import org.archive.wayback.cdx.filter.UrlPrefixFilter;
import org.archive.wayback.cdx.filter.WindowEndFilter;
import org.archive.wayback.cdx.filter.WindowStartFilter;
import org.archive.wayback.cdx.indexer.IndexPipeline;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;

import com.sleepycat.je.DatabaseException;

/**
 * Implements ResourceIndex interface using a BDBResourceIndex
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class LocalBDBResourceIndex implements ResourceIndex {
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER =
	        Logger.getLogger(LocalBDBResourceIndex.class.getName());


	/**
	 * configuration name for directory containing index
	 */
	private final static String INDEX_PATH = "resourceindex.indexpath";

	/**
	 * configuration name for BDBJE database name within the db directory
	 */
	private final static String DB_NAME = "resourceindex.dbname";

	/**
	 * configuration name for URL prefix to access exclusion service
	 */
	private final static String EXCLUSION_PREFIX = "resourceindex.exclusionurl";

	/**
	 * configuration name for User Agent to send to exclusion service
	 */
	private final static String EXCLUSION_UA = "resourceindex.exclusionua";

	
	// TODO: add configuration for MAX_RECORDS
	/**
	 * maximum number of records to return
	 */
	private final static int MAX_RECORDS = 1000;

	/**
	 * BDBResourceIndex object
	 */
	private BDBResourceIndex db = null;

	/**
	 * IndexPipeline object
	 */
	private IndexPipeline pipeline = null;

	private String exclusionUrlPrefix = null;
	
	private String exclusionUserAgent = null;
	
	
	/**
	 * Constructor
	 */
	public LocalBDBResourceIndex() {
		super();
	}

	public void init(Properties p) throws ConfigurationException {
		LOGGER.info("initializing LocalDBDResourceIndex...");
		String dbPath = (String) p.get(INDEX_PATH);
		if (dbPath == null || (dbPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + INDEX_PATH);
		}
		String dbName = (String) p.get(DB_NAME);
		if (dbName == null || (dbName.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + DB_NAME);
		}

		exclusionUrlPrefix = (String) p.get(EXCLUSION_PREFIX);

		exclusionUserAgent = (String) p.get(EXCLUSION_UA);
		
		try {
			db = new BDBResourceIndex(dbPath, dbName);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
		pipeline = new IndexPipeline();
		pipeline.init(p);
	}
	
	private String getRequired(WaybackRequest wbRequest, String field)
		throws BadQueryException {

		String value = wbRequest.get(field);
		if(value == null) {
			throw new BadQueryException("No " + field + " specified");
		}
		return value;
	}
	
	/**
	 * @param wbRequest
	 * @return SearchResults matching request
	 * @throws ResourceIndexNotAvailableException
	 * @throws ResourceNotInArchiveException
	 * @throws BadQueryException
	 * @throws AccessControlException 
	 */
	public SearchResults query(WaybackRequest wbRequest)
		throws ResourceIndexNotAvailableException,
		ResourceNotInArchiveException, BadQueryException,
		AccessControlException {

		
		String startKey;              // actual BDB key where search will begin
		String keyUrl;                // "purified" URL request
		SearchResults results = null; // return value placeholder
		int startResult;              // calculated based on hits/page + pagenum
		
		// first grab all the info from the WaybackRequest, and validate it:
		
		int resultsPerPage = wbRequest.getResultsPerPage();
		int pageNum = wbRequest.getPageNum();
		startResult = (pageNum - 1) * resultsPerPage;

		if (resultsPerPage < 1) {
			throw new BadQueryException("resultsPerPage cannot be < 1");
		}
		if (resultsPerPage > MAX_RECORDS) {
			throw new BadQueryException("resultsPerPage cannot be > "
					+ MAX_RECORDS);
		}
		if(pageNum < 1) {
			throw new BadQueryException("pageNum must be > 0");
		}
		
		String searchUrl = getRequired(wbRequest,
				WaybackConstants.REQUEST_URL);
		String searchType = getRequired(wbRequest,
				WaybackConstants.REQUEST_TYPE);
		String startDate = getRequired(wbRequest,
				WaybackConstants.REQUEST_START_DATE);
		String endDate = getRequired(wbRequest,
				WaybackConstants.REQUEST_END_DATE);

		try {
			keyUrl = CDXRecord.urlStringToKey(searchUrl);
		} catch (URIException e) {
			throw new BadQueryException("invalid " + 
					WaybackConstants.REQUEST_URL + " " + searchUrl);
		}
		
		// build up the FilterChain:
		FilterChain filters = new FilterChain();
		// first the guardrail to keep us from inspecting too many records:
		filters.addFilter(new GuardRailFilter(MAX_RECORDS));
		if (searchType.equals(WaybackConstants.REQUEST_REPLAY_QUERY)) {

			filters.addFilter(new UrlMatchFilter(keyUrl));
			filters.addFilter(new EndDateFilter(endDate));
			startKey = keyUrl + " " + startDate;

		} else if (searchType.equals(WaybackConstants.REQUEST_URL_QUERY)) {

			filters.addFilter(new UrlMatchFilter(keyUrl));
			filters.addFilter(new EndDateFilter(endDate));
			startKey = keyUrl + " " + startDate;
			
		} else if (searchType.equals(
				WaybackConstants.REQUEST_URL_PREFIX_QUERY)) {

			
			filters.addFilter(new UrlPrefixFilter(keyUrl));
			filters.addFilter(new DateRangeFilter(startDate,endDate));

			startKey = keyUrl;

		} else {
			throw new BadQueryException("Unknown query type, must be " +
					WaybackConstants.REQUEST_REPLAY_QUERY + ", " +
					WaybackConstants.REQUEST_URL_QUERY + ", or " +
					WaybackConstants.REQUEST_URL_PREFIX_QUERY);
		}
		
		ExclusionFilter exclusion = null;
		if(exclusionUrlPrefix != null) {
			// throw in the ExclusionFilter:
			exclusion = getExclusionFilter();
			filters.addFilter(exclusion);
		}
		
		// throw in a counter to see how many results total matched:
		CounterFilter counter = new CounterFilter();
		filters.addFilter(counter);
		
		// add the start and end windowing filters:
		RecordFilter windowStart = new WindowStartFilter(startResult);
		RecordFilter windowEnd = new WindowEndFilter(resultsPerPage);
		filters.addFilter(windowStart);
		filters.addFilter(windowEnd);
		
		
		results = db.filterRecords(startKey,filters);
		
		int matched = counter.getNumMatched();
		if(matched == 0) {
			if(exclusion != null && exclusion.blockedAll()) {
				throw new AccessControlException("All results Excluded");
			}
			throw new ResourceNotInArchiveException("the URL " + keyUrl + 
			" is not in the archive.");
		}
		
		// now we need to set some filter properties on the results:
		results.putFilter(WaybackConstants.REQUEST_URL,keyUrl);
		results.putFilter(WaybackConstants.REQUEST_TYPE,searchType);
		results.putFilter(WaybackConstants.REQUEST_START_DATE,startDate);
		results.putFilter(WaybackConstants.REQUEST_END_DATE,endDate);

		// window info
		results.putFilter(WaybackConstants.RESULTS_FIRST_RETURNED,
				""+startResult);
		results.putFilter(WaybackConstants.RESULTS_REQUESTED,
				""+resultsPerPage);

		// how many are actually in the results:
		results.putFilter(WaybackConstants.RESULTS_NUM_RESULTS,""+matched);

		// how many matched (includes those outside window)
		results.putFilter(WaybackConstants.RESULTS_NUM_RETURNED,
				""+results.getResultCount());
		
		return results;
	}	
	
	private ExclusionFilter getExclusionFilter() {
		return new ExclusionFilter(exclusionUrlPrefix,exclusionUserAgent);
	}
}
