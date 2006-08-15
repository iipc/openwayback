/* FileLocationDB
 *
 * $Id$
 *
 * Created on 3:50:00 PM Dec 14, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.http11resourcestore;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

import org.archive.wayback.PropertyConfigurable;
import org.archive.wayback.exception.ConfigurationException;

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
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileLocationDB implements PropertyConfigurable {
	private static final Logger LOGGER = Logger.getLogger(
			FileLocationDB.class.getName());

	/**
	 * String id for implementation class of FileLocationDBs.
	 */
	public static final String FILE_LOCATION_DB_CLASS = "filelocationdb";
	private static final String ARC_DB_PATH = "filelocationdb.path";

	private static final String ARC_DB_NAME = "filelocationdb.name";


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

	private final static String urlDelimiter = " ";
	
	private final static String urlDelimiterRE = " ";
	
	/**
	 * Constructor
	 */
	public FileLocationDB() {
		super();
	}
	
	public void init(Properties p) throws ConfigurationException {
		String dbPath = (String) p.get(ARC_DB_PATH);
		if ((dbPath == null) || (dbPath.length() < 1)) {
			throw new ConfigurationException("Failed to find " + ARC_DB_PATH);
		}
		String dbName = (String) p.get(ARC_DB_NAME);
		if ((dbName == null) || (dbName.length() < 1)) {
			throw new ConfigurationException("Failed to find " + ARC_DB_NAME);
		}

		try {

			File dbDir = new File(dbPath);
			if(!dbDir.exists()) {
				if(!dbDir.mkdirs()) {
					throw new ConfigurationException("Failed to create " + dbPath);
				}
			}
			
			LOGGER.info("Initializing FileLocationDB at(" + dbPath + 
					") named(" + dbName + ")");
			
			initializeDB(dbPath, dbName);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
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
		environmentConfig.setTransactional(true);
		environmentConfig.setConfigParam("je.log.fileMax",JE_LOG_FILEMAX);
		File file = new File(path);
		env = new Environment(file, environmentConfig);
		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setAllowCreate(true);
		databaseConfig.setTransactional(true);
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
	 * @return Returns the dbName.
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}

	private byte[] stringToBytes(String s) {
		try {
			return s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// no UTF-8, huh?
			e.printStackTrace();
			return s.getBytes();
		}
	}
	private String bytesToString(byte[] ba) {
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// not likely..
			e.printStackTrace();
			return new String(ba);
		}
	}
	
	/**
	 * return an array of String URLs for all known locations of the ARC file
	 * in the DB.
	 * @param arcName
	 * @return String[] of URLs to arcName
	 * @throws DatabaseException
	 */
	public String[] arcToUrls(final String arcName) throws DatabaseException {
		
		String[] arcUrls = null;
		
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		key.setData(stringToBytes(arcName));
		key.setPartial(false);
		Cursor cursor = db.openCursor(null, null);
		OperationStatus status = cursor.getSearchKey(key, value,
				LockMode.DEFAULT);
		if (status == OperationStatus.SUCCESS) {
			String valueString = bytesToString(value.getData());
			if(valueString != null && valueString.length() > 0) {
				arcUrls = valueString.split(urlDelimiterRE);
			}
		}
		cursor.close();
		return arcUrls;
	}
	
	/**
	 * add an Url location for an arcName, unless it already exists
	 * @param arcName
	 * @param arcUrl
	 * @throws DatabaseException
	 */
	public void addArcUrl(final String arcName, final String arcUrl) throws DatabaseException {
		
		// need to first see if there is already an entry for this arcName.
		// if not, add arcUrl as the value.
		// if so, check the current arcUrl locations for arcName
		//     if arcUrl exists, do nothing
		//     if arcUrl does not exist, add, and set that as the value.
		
		String newValue = null;
		
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		
		key.setData(stringToBytes(arcName));
		key.setPartial(false);

		OperationStatus status = db.get(null,key, value,LockMode.DEFAULT);
		if (status == OperationStatus.SUCCESS) {
			String valueString = bytesToString(value.getData());
			if(valueString != null && valueString.length() > 0) {
				String curUrls[] = valueString.split(urlDelimiterRE);
				boolean found = false;
				for(int i=0; i < curUrls.length; i++) {
					if(arcUrl.equals(curUrls[i])) {
						found = true;
						break;
					}
				}
				if(found == false) {
					newValue = valueString + " " + arcUrl;
				}
			} else {
				// null or empty value
				newValue = arcUrl;
			}
		} else {
			
			// no current value:
			newValue = arcUrl;
		
		}
		
		// did we find a value?
		if(newValue != null) {
			value.setData(stringToBytes(newValue));
			status = db.put(null,key, value);
			if (status != OperationStatus.SUCCESS) {
				throw new DatabaseException("oops, put had non-success status");
			}
		}
	}

	/**
	 * remove a single Url location for an arcName, if it exists
	 * @param arcName
	 * @param arcUrl
	 * @throws DatabaseException
	 */
	public void removeArcUrl(final String arcName, final String arcUrl) throws DatabaseException {
		// need to first see if there is already an entry for this arcName.
		// if not, do nothing
		// if so, loop thru all current arcUrl locations for arcName
		//     keep any that are not arcUrl
		// if any locations are left, update to the new value, sans arcUrl
		// if none are left, remove the entry from the db
		
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		
		//Cursor cursor = db.openCursor(null, null);
		key.setData(stringToBytes(arcName));
		key.setPartial(false);

		OperationStatus status = db.get(null,key, value,LockMode.DEFAULT);
		if (status == OperationStatus.SUCCESS) {
			StringBuilder newValue = new StringBuilder(300);

			String valueString = bytesToString(value.getData());
			if(valueString != null && valueString.length() > 0) {
				String curUrls[] = valueString.split(urlDelimiterRE);

				for(int i=0; i < curUrls.length; i++) {
					if(!arcUrl.equals(curUrls[i])) {
						if(newValue.length() > 0) {
							newValue.append(urlDelimiter);
						}
						newValue.append(curUrls[i]);
					}
				}
			}
			
			if(newValue.length() > 0) {
				
				// update
				value.setData(stringToBytes(newValue.toString()));
				status = db.put(null,key, value);
				if (status != OperationStatus.SUCCESS) {
					throw new DatabaseException("oops, put had non-success status");
				}
				
			} else {
				
				// remove the entry:
				status = db.delete(null,key);
				if (status != OperationStatus.SUCCESS) {
					throw new DatabaseException("oops, delete had non-success status");
				}
			}
		}
	}
}
