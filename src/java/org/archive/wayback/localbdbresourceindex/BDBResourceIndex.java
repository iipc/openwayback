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

package org.archive.wayback.localbdbresourceindex;

import java.io.File;
import java.text.ParseException;
import java.util.Iterator;

import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;

// import com.sleepycat.bind.tuple.TupleBinding;
// import com.sleepycat.bind.tuple.TupleInput;
// import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
// import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
// import com.sleepycat.je.DatabaseNotFoundException;
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
	private String path;

	private String dbName;

	Environment env = null;

	Database db = null;

	// Cursor cursor = null;

	/**
	 * Constructor
	 * 
	 * @param thePath
	 *            directory where BDBJE files are stored
	 * @param theDbName
	 *            name of BDB database
	 * @throws Exception
	 */
	public BDBResourceIndex(final String thePath, final String theDbName)
			throws Exception {
		super();
		initializeDB(thePath, theDbName);
	}

	protected void initializeDB(final String thePath, final String theDbName)
			throws Exception {
		path = thePath;
		dbName = theDbName;

		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);
		environmentConfig.setTransactional(false);
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

	protected ResourceResults doUrlSearch(final String url,
			final String firstDate, final String lastDate,
			final String exactHost, final int maxRecords) {
		ResourceResults results = new ResourceResults();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		int numRecords = 0;

		String searchStart = url + " " + firstDate;
		key.setData(searchStart.getBytes());
		key.setPartial(false);
		try {
			Cursor cursor = db.openCursor(null, null);
			OperationStatus status = cursor.getSearchKeyRange(key, value,
					LockMode.DEFAULT);
			while (status == OperationStatus.SUCCESS) {
				// String keyString = new String(key.getData());

				String valueString = new String(value.getData());
				ResourceResult result = new ResourceResult();
				result.parseLine(valueString, 0);
				if (!result.getUrl().equals(url)) {
					break;
				}
				if (result.getTimestamp().getDateStr().compareTo(lastDate) > 0) {
					break;
				}
				if (result.getTimestamp().getDateStr().compareTo(firstDate) >= 0) {
					results.addResourceResult(result);
					numRecords++;
					if (numRecords >= maxRecords) {
						break;
					}
				}
				status = cursor.getNext(key, value, LockMode.DEFAULT);
			}
			cursor.close();
		} catch (DatabaseException dbe) {
			dbe.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return results;
	}

	protected ResourceResults doUrlPrefixSearch(final String urlPrefix,
			final String firstDate, final String lastDate,
			final String exactHost, final int maxRecords) {
		ResourceResults results = new ResourceResults();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		int numRecords = 0;

		String searchStart = urlPrefix;
		key.setData(searchStart.getBytes());
		key.setPartial(false);
		try {
			Cursor cursor = db.openCursor(null, null);
			OperationStatus status = cursor.getSearchKeyRange(key, value,
					LockMode.DEFAULT);
			while (status == OperationStatus.SUCCESS) {

				String valueString = new String(value.getData());
				ResourceResult result = new ResourceResult();
				result.parseLine(valueString, 0);
				if (!result.getUrl().startsWith(urlPrefix)) {
					break;
				}
				if ((result.getTimestamp().getDateStr().compareTo(lastDate) <= 0)
						&& (result.getTimestamp().getDateStr().compareTo(
								firstDate) >= 0)) {
					results.addResourceResult(result);
					numRecords++;
					if (numRecords >= maxRecords) {
						break;
					}
				}
				status = cursor.getNext(key, value, LockMode.DEFAULT);
			}
			cursor.close();
		} catch (DatabaseException dbe) {
			dbe.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * Add all ResourceResult in results to BDB index
	 * @param results
	 * @throws Exception
	 */
	public void addResults(ResourceResults results) throws Exception {
		Iterator itr = results.iterator();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		OperationStatus status = null;
		try {
			Cursor cursor = db.openCursor(null, null);
			while (itr.hasNext()) {
				ResourceResult result = (ResourceResult) itr.next();
				String keyString = result.getUrl() + " "
						+ result.getTimestamp().getDateStr();
				String valueString = result.toString();
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
