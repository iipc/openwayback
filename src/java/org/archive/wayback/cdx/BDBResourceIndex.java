/* BDBResourceIndex
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
import java.text.ParseException;
import java.util.Iterator;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * ResourceResults-specific wrapper on top of the BDBJE database.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class BDBResourceIndex {
	/**
	 * Maximum BDBJE file size
	 */
	private final static String JE_LOG_FILEMAX = "256000000";
	/**
	 * path to directory containing the BDBJE files
	 */
	private String path;

	/**
	 * name of BDBJE db within the path directory
	 */
	private String dbName;

	/**
	 * BDBJE Environment
	 */
	Environment env = null;

	/**
	 * BDBJE Database
	 */
	Database db = null;

	/**
	 * Constructor
	 * 
	 * @param thePath
	 *            directory where BDBJE files are stored
	 * @param theDbName
	 *            name of BDB database
	 * @throws DatabaseException 
	 */
	public BDBResourceIndex(final String thePath, final String theDbName)
			throws DatabaseException {
		super();
		initializeDB(thePath, theDbName);
	}

	/**
	 * @param thePath Directory where BDBJE files are stored
	 * @param theDbName Name of files in thePath
	 * @throws DatabaseException
	 */
	protected void initializeDB(final String thePath, final String theDbName)
			throws DatabaseException {
		path = thePath;
		dbName = theDbName;

		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);
		environmentConfig.setTransactional(false);
		environmentConfig.setConfigParam("je.log.fileMax",JE_LOG_FILEMAX);
		File file = new File(path);
		env = new Environment(file, environmentConfig);
		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setAllowCreate(true);
		databaseConfig.setTransactional(false);
		// perform other database configurations

		db = env.openDatabase(null, dbName, databaseConfig);
	}

	/**
	 * shut down the BDB.
	 * 
	 * @throws DatabaseException
	 */
	public void shutdownDB() throws DatabaseException {

		if (db != null) {
			db.close();
		}

		if (env != null) {
			env.close();
		}
	}

	/**
	 * @param url filter results which do not match this url
	 * @param firstDate filter results before this date
	 * @param lastDate filter results after this date
	 * @param exactHost filter records not from this specific host
	 * @param startRecord filter records before this (0 based)
	 * @param maxRecords return at most this many records
	 * @return SearchResults matching the filters
	 */
	// TODO add aditional "replay" search method which allows passing in of 
	// an exact date, and use a "scrolling window" of the best results, to 
	// allow for returning the N closest results to a particular date, within
	// a specific window of dates...
	
	protected SearchResults doUrlSearch(final String url,
			final String firstDate, final String lastDate,
			final String exactHost, final int startRecord,
			final int maxRecords) {

		SearchResults results = new SearchResults();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		int numScanned = 0;
		int numSkipped = 0;
		int numAdded = 0;
		int numMatching = 0;
		int maxScanRecords = 10000;

		String searchStart = url + " " + firstDate;
		key.setData(searchStart.getBytes());
		key.setPartial(false);
		try {
			Cursor cursor = db.openCursor(null, null);
			OperationStatus status = cursor.getSearchKeyRange(key, value,
					LockMode.DEFAULT);
			while (status == OperationStatus.SUCCESS) {

				// safety catch -- keep us from grinding ourselves too badly
				// on a single request...
				numScanned++;
				if (numScanned > maxScanRecords) {
					//results.putFilter(WaybackConstants.RESULTS_HAS_MORE,
					//		"true");
					break;
				}
				
				// String keyString = new String(key.getData());

				String valueString = new String(value.getData());
				CDXRecord parser = new CDXRecord();
				parser.parseLine(valueString, 0);

				if (!parser.url.equals(url)) {
					break;
				}
				if (parser.captureDate.compareTo(lastDate) > 0) {
					break;
				}
				if (parser.captureDate.compareTo(firstDate) >= 0) {
					numMatching++;
					if (numSkipped >= startRecord) {
						if(numAdded < maxRecords) {
							results.addSearchResult(parser.toSearchResult());
							numAdded++;
						}
					} else {
						numSkipped++;
					}
				}
				status = cursor.getNext(key, value, LockMode.DEFAULT);
			}
			results.putFilter(WaybackConstants.RESULTS_FIRST_RETURNED,
					""+startRecord);
			results.putFilter(WaybackConstants.RESULTS_NUM_RESULTS,
					""+numMatching);
			results.putFilter(WaybackConstants.RESULTS_NUM_RETURNED,
					""+numAdded);
			results.putFilter(WaybackConstants.RESULTS_REQUESTED,
					""+maxRecords);
			
			cursor.close();
		} catch (DatabaseException dbe) {
			// TODO: let this bubble up as Index error
			dbe.printStackTrace();
		} catch (ParseException e) {
			// TODO: let this bubble up as Index error
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * @param urlPrefix filter results which do not match this urlPrefix
	 * @param firstDate filter results before this date
	 * @param lastDate filter results after this date
	 * @param exactHost filter records not from this specific host
	 * @param startRecord filter records before this (0 based)
	 * @param maxRecords return at most this many records
	 * @return SearchResults matching the filters
	 */
	protected SearchResults doUrlPrefixSearch(final String urlPrefix,
			final String firstDate, final String lastDate,
			final String exactHost, final int startRecord,
			final int maxRecords) {

		SearchResults results = new SearchResults();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		int numScanned = 0;
		int numSkipped = 0;
		int numAdded = 0;
		int numMatching = 0;
		int maxScanRecords = 1000;

		String searchStart = urlPrefix;
		key.setData(searchStart.getBytes());
		key.setPartial(false);
		try {
			Cursor cursor = db.openCursor(null, null);
			OperationStatus status = cursor.getSearchKeyRange(key, value,
					LockMode.DEFAULT);
			while (status == OperationStatus.SUCCESS) {

				// safety catch -- keep us from grinding ourselves too badly
				// on a single request...
				numScanned++;
				if (numScanned > maxScanRecords) {
					//results.putFilter(WaybackConstants.RESULTS_HAS_MORE,
					//		"true");
					break;
				}

				String valueString = new String(value.getData());
				CDXRecord parser = new CDXRecord();
				parser.parseLine(valueString, 0);

				if (!parser.url.startsWith(urlPrefix)) {
					break;
				}
				if ((parser.captureDate.compareTo(lastDate) <= 0)
						&& (parser.captureDate.compareTo(firstDate) >= 0)) {
					numMatching++;
					if (numSkipped >= startRecord) {
						if(numAdded < maxRecords) {
							results.addSearchResult(parser.toSearchResult());
							numAdded++;
						}
					} else {
						numSkipped++;
					}
				}
				status = cursor.getNext(key, value, LockMode.DEFAULT);
			}
			results.putFilter(WaybackConstants.RESULTS_FIRST_RETURNED,
					""+startRecord);
			results.putFilter(WaybackConstants.RESULTS_NUM_RESULTS,
					""+numMatching);
			results.putFilter(WaybackConstants.RESULTS_NUM_RETURNED,
					""+numAdded);
			results.putFilter(WaybackConstants.RESULTS_REQUESTED,
					""+maxRecords);
			
			cursor.close();
		} catch (DatabaseException dbe) {
			// TODO: let this bubble up as Index error
			dbe.printStackTrace();
		} catch (ParseException e) {
			// TODO: let this bubble up as Index error
			e.printStackTrace();
		}
	
		return results;
	}

	/**
	 * Add all ResourceResult in results to BDB index
	 * @param results
	 * @throws Exception
	 */
	public void addResults(SearchResults results) throws Exception {
		Iterator itr = results.iterator();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		OperationStatus status = null;
		CDXRecord parser = new CDXRecord();
		try {
			Cursor cursor = db.openCursor(null, null);
			while (itr.hasNext()) {
				SearchResult result = (SearchResult) itr.next();
				parser.fromSearchResult(result);
				String keyString = parser.toKey();
				String valueString = parser.toValue();
				key.setData(keyString.getBytes());
				value.setData(valueString.getBytes());
				status = cursor.put(key, value);
				if (status != OperationStatus.SUCCESS) {
					throw new Exception("oops, put had non-success status");
				}
			}
			cursor.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}


}
