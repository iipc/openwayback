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

import java.io.File;
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
import org.archive.wayback.cdx.filter.StartDateFilter;
import org.archive.wayback.cdx.filter.UrlMatchFilter;
import org.archive.wayback.cdx.filter.UrlPrefixFilter;
import org.archive.wayback.cdx.filter.WindowEndFilter;
import org.archive.wayback.cdx.filter.WindowStartFilter;
import org.archive.wayback.cdx.indexer.IndexPipeline;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
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

	/**
	 * Name of configuration for flag to activate pipeline thread
	 */
	private final static String RUN_PIPELINE = "indexpipeline.runpipeline";

	
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
			String runPipeline = (String) p.get(RUN_PIPELINE);
			if ((runPipeline != null) && (runPipeline.equals("1"))) {
				File dbDir = new File(dbPath);
				if (!dbDir.isDirectory() && !dbDir.mkdirs()) {
					throw new ConfigurationException("FAILED to create " + dbPath);
				}
				db = new BDBResourceIndex(dbPath, dbName, false);
				pipeline = new IndexPipeline(db,true);
			} else {
				db = new BDBResourceIndex(dbPath, dbName, true);
				pipeline = new IndexPipeline(db,false);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
		pipeline.init(p);
	}

	private String getRequired(WaybackRequest wbRequest, String field, 
			String defaultValue)
	throws BadQueryException {
		
		String value = wbRequest.get(field);
		if(value == null) {
			if(defaultValue == null) {
				throw new BadQueryException("No " + field + " specified");
			} else {
				value = defaultValue;
			}
		}
		return value;		
	}	
	private String getRequired(WaybackRequest wbRequest, String field)
		throws BadQueryException {
		return getRequired(wbRequest,field,null);
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

		
		SearchResults results = new SearchResults(); // return value placeholder
		
		String startKey;              // actual BDB key where search will begin
		String keyUrl;                // "purified" URL request
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
				WaybackConstants.REQUEST_START_DATE,
				Timestamp.earliestTimestamp().getDateStr());
		String endDate = getRequired(wbRequest,
				WaybackConstants.REQUEST_END_DATE,
				Timestamp.latestTimestamp().getDateStr());
		String exactDate = getRequired(wbRequest,
				WaybackConstants.REQUEST_EXACT_DATE,
				Timestamp.latestTimestamp().getDateStr());

		try {
			keyUrl = CDXRecord.urlStringToKey(searchUrl);
		} catch (URIException e) {
			throw new BadQueryException("invalid " + 
					WaybackConstants.REQUEST_URL + " " + searchUrl);
		}

		
		// set up the common Filters:

		// makes sure we don't inspect too many records: prevents DOS
		GuardRailFilter guardrail = new GuardRailFilter(MAX_RECORDS);

		// checks an exclusion service for every matching record
		ExclusionFilter exclusion = getExclusionFilter();
		
		// this filter will just count how many results matched:
		CounterFilter counter = new CounterFilter();

		if (searchType.equals(WaybackConstants.REQUEST_REPLAY_QUERY)
				|| searchType.equals(WaybackConstants.REQUEST_CLOSEST_QUERY)) {

			FilterChain forward = new FilterChain();
			FilterChain reverse = new FilterChain();
			
			 // use the same guardrail for both:
			forward.addFilter(guardrail);
			reverse.addFilter(guardrail);
			
			// match URL key:
			forward.addFilter(new UrlMatchFilter(keyUrl));
			reverse.addFilter(new UrlMatchFilter(keyUrl));
			
			// stop matching if we hit a date outside the search range:
			forward.addFilter(new EndDateFilter(endDate));
			reverse.addFilter(new StartDateFilter(startDate));
			
			// possibly filter via exclusions:
			if(exclusion != null) {
				forward.addFilter(exclusion);
				reverse.addFilter(exclusion);
			}
			
			int resultsPerDirection = (int) Math.floor(resultsPerPage / 2);
			if(resultsPerDirection * 2 == resultsPerPage) {
				forward.addFilter(new WindowEndFilter(resultsPerDirection));
			} else {
				forward.addFilter(new WindowEndFilter(resultsPerDirection + 1));				
			}
			reverse.addFilter(new WindowEndFilter(resultsPerDirection));
			
			// add the same counter:
			forward.addFilter(counter);
			reverse.addFilter(counter);
			
			startKey = keyUrl + " " + exactDate;
			
			// first the reverse search:
			db.filterRecords(startKey,reverse,results,false);
			// then the forwards:
			db.filterRecords(startKey,forward,results,true);

		} else if (searchType.equals(WaybackConstants.REQUEST_URL_QUERY)) {

			// build up the FilterChain(s):
			FilterChain filters = new FilterChain();
			filters.addFilter(guardrail);

			filters.addFilter(new UrlMatchFilter(keyUrl));
			filters.addFilter(new EndDateFilter(endDate));
			// possibly filter via exclusions:
			if(exclusion != null) {
				filters.addFilter(exclusion);
			}
			startKey = keyUrl + " " + startDate;
			filters.addFilter(counter);

			// add the start and end windowing filters:
			filters.addFilter(new WindowStartFilter(startResult));
			filters.addFilter(new WindowEndFilter(resultsPerPage));

			db.filterRecords(startKey,filters,results,true);
			
		} else if (searchType.equals(
				WaybackConstants.REQUEST_URL_PREFIX_QUERY)) {

			// build up the FilterChain(s):
			FilterChain filters = new FilterChain();
			filters.addFilter(guardrail);

			filters.addFilter(new UrlPrefixFilter(keyUrl));
			filters.addFilter(new DateRangeFilter(startDate,endDate));

			// possibly filter via exclusions:
			if(exclusion != null) {
				filters.addFilter(exclusion);
			}
			startKey = keyUrl;
			filters.addFilter(counter);

			// add the start and end windowing filters:
			filters.addFilter(new WindowStartFilter(startResult));
			filters.addFilter(new WindowEndFilter(resultsPerPage));

			db.filterRecords(startKey,filters,results,true);
		


		} else {
			throw new BadQueryException("Unknown query type("+searchType+"), must be " +
					WaybackConstants.REQUEST_REPLAY_QUERY + ", " +
					WaybackConstants.REQUEST_CLOSEST_QUERY + ", " +
					WaybackConstants.REQUEST_URL_QUERY + ", or " +
					WaybackConstants.REQUEST_URL_PREFIX_QUERY);
		}

		
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
		results.putFilter(WaybackConstants.REQUEST_EXACT_DATE,exactDate);
		results.putFilter(WaybackConstants.REQUEST_END_DATE,endDate);

		// window info
		results.putFilter(WaybackConstants.RESULTS_FIRST_RETURNED,
				String.valueOf(startResult));
		results.putFilter(WaybackConstants.RESULTS_REQUESTED,
				String.valueOf(resultsPerPage));

		// how many are actually in the results:
		results.putFilter(WaybackConstants.RESULTS_NUM_RESULTS,
				String.valueOf(matched));

		// how many matched (includes those outside window)
		results.putFilter(WaybackConstants.RESULTS_NUM_RETURNED,
				String.valueOf(results.getResultCount()));
		
		return results;
	}	
	
	private ExclusionFilter getExclusionFilter() {
		if(exclusionUrlPrefix != null) {
			return new ExclusionFilter(exclusionUrlPrefix,exclusionUserAgent);
		}
		return null;
	}
}
