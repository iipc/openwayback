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

import java.text.ParseException;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.cdx.indexer.IndexPipeline;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
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
		try {
			db = new BDBResourceIndex(dbPath, dbName);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
		pipeline = new IndexPipeline();
		pipeline.init(p);
	}

	public SearchResults query(WaybackRequest wbRequest)
			throws ResourceIndexNotAvailableException,
			ResourceNotInArchiveException, BadQueryException {

		UURI searchURI;
		String searchHost;
		String searchPath;

		int resultsPerPage = wbRequest.getResultsPerPage();
		int pageNum = wbRequest.getPageNum();
		int startResult;

		String searchUrl = wbRequest.get(WaybackConstants.REQUEST_URL);
		String searchType = wbRequest.get(WaybackConstants.REQUEST_TYPE);
		String startDate = wbRequest.get(WaybackConstants.REQUEST_START_DATE);
		String endDate = wbRequest.get(WaybackConstants.REQUEST_END_DATE);

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
		startResult = (pageNum - 1) * resultsPerPage;

		if ((searchUrl == null) || (searchUrl.length() == 0)) {
			throw new BadQueryException(WaybackConstants.REQUEST_URL +
					" must be specified");
		}
		if ((searchType == null) || (searchType.length() == 0)) {
			throw new BadQueryException(WaybackConstants.REQUEST_TYPE +
					" must be specified");
		}

		if ((startDate == null) || (startDate.length() == 0)) {
			try {
				startDate = Timestamp.earliestTimestamp().getDateStr();
			} catch (ParseException e) {
				e.printStackTrace();
				throw new BadQueryException("unexpected data error " 
						+ e.getMessage());
			}
		}
		if ((endDate == null) || (endDate.length() == 0)) {
			try {
				endDate = Timestamp.currentTimestamp().getDateStr();
			} catch (ParseException e) {
				e.printStackTrace();
				throw new BadQueryException("unexpected data error " 
						+ e.getMessage());
			}
		}

		try {

			if (searchUrl.startsWith("http://")) {
                    if (-1 == searchUrl.indexOf('/', 8)) {
                    	searchUrl = searchUrl + "/";
                    }
            } else {
                    if (-1 == searchUrl.indexOf("/")) {
                    	searchUrl = searchUrl + "/";
                    }
                    searchUrl = "http://" + searchUrl;
            }

			searchURI = UURIFactory.getInstance(searchUrl);
			searchHost = searchURI.getHostBasename();
			searchPath = searchURI.getEscapedPathQuery();

		} catch (URIException e) {
			e.printStackTrace();
			throw new BadQueryException("Problem with URI " + e.getMessage());
		}

		String keyUrl = searchHost + searchPath;
		SearchResults results;
		if (searchType.equals(WaybackConstants.REQUEST_REPLAY_QUERY)) {

			results = db.doUrlSearch(keyUrl, startDate, endDate, null,
					startResult, resultsPerPage);

		} else if (searchType.equals(WaybackConstants.REQUEST_URL_QUERY)) {

			results = db.doUrlSearch(keyUrl, startDate, endDate, null,
					startResult, resultsPerPage);

		} else if (searchType.equals(
				WaybackConstants.REQUEST_URL_PREFIX_QUERY)) {

			results = db.doUrlPrefixSearch(keyUrl, startDate, endDate, null,
					startResult, resultsPerPage);

		} else {
			throw new BadQueryException("Unknown query type, must be " +
					WaybackConstants.REQUEST_REPLAY_QUERY + ", " +
					WaybackConstants.REQUEST_URL_QUERY + ", or " +
					WaybackConstants.REQUEST_URL_PREFIX_QUERY);
		}
		if(results.isEmpty()) {
			throw new ResourceNotInArchiveException("the URL " + keyUrl + 
					" is not in the archive.");
		}
		results.putFilter(WaybackConstants.REQUEST_URL,keyUrl);
		results.putFilter(WaybackConstants.REQUEST_TYPE,searchType);
		results.putFilter(WaybackConstants.REQUEST_START_DATE,startDate);
		results.putFilter(WaybackConstants.REQUEST_END_DATE,endDate);

		return results;
	}
}
