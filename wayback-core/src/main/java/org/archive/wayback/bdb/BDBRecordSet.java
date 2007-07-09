/* BDBRecordGenerator
 *
 * $Id$
 *
 * Created on 1:22:39 PM May 15, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
package org.archive.wayback.bdb;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBRecordSet {
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
	 * @param thePath Directory where BDBJE files are stored
	 * @param theDbName Name of files in thePath
	 * @throws DatabaseException
	 */
	public void initializeDB(final String thePath, final String theDbName)
			throws DatabaseException {
		path = thePath;
		dbName = theDbName;

		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);
		environmentConfig.setTransactional(true);
		environmentConfig.setConfigParam("je.log.fileMax",JE_LOG_FILEMAX);
		File file = new File(path);
		if(!file.isDirectory()) {
			if(!file.mkdirs()) {
				throw new DatabaseException("failed mkdirs(" + path + ")");
			}
		}
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
	public synchronized void shutdownDB() throws DatabaseException {

		if (db != null) {
			db.close();
			db = null;
		}

		if (env != null) {
			env.close();
			env = null;
		}
	}
	
	/**
	 * @param s
	 * @return byte array representation of String s in UTF-8
	 */
	public static byte[] stringToBytes(String s) {
		try {
			return s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// no UTF-8, huh?
			e.printStackTrace();
			return s.getBytes();
		}
	}
	/**
	 * @param ba
	 * @return String of UTF-8 encoded bytes ba
	 */
	public static String bytesToString(byte[] ba) {
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// not likely..
			e.printStackTrace();
			return new String(ba);
		}
	}

	/**
	 * @param startKey
	 * @return iterator for BDBRecords
	 * @throws DatabaseException
	 */
	public BDBRecordIterator recordIterator(final String startKey)
	throws DatabaseException {
		return recordIterator(startKey,true);
	}
	/**
	 * @param startKey 
	 * @param forward 
	 * @return iterator for BDBRecords
	 * @throws DatabaseException 
	 */
	public BDBRecordIterator recordIterator(final String startKey, 
		final boolean forward) throws DatabaseException {
		Cursor cursor = db.openCursor(null, null);
		return new BDBRecordIterator(cursor,startKey,!forward);
	}

	/**
	 * @param itr
	 */
	public void insertRecords(final Iterator<BDBRecord> itr) {
		OperationStatus status = null;
		try {
			Transaction txn = env.beginTransaction(null, null);
			try {
				Cursor cursor = db.openCursor(txn, null);
				while (itr.hasNext()) {
					BDBRecord record = (BDBRecord) itr.next();
					status = cursor.put(record.getKey(), record.getValue());
					if (status != OperationStatus.SUCCESS) {
						throw new RuntimeException("put() non-success status");
					}
				}
				cursor.close();
				txn.commit();
			} catch (DatabaseException e) {
				if(txn != null) {
					txn.abort();
				}
				e.printStackTrace();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * persistantly store key-value pair 
	 * @param keyStr
	 * @param valueStr
	 * @throws DatabaseException 
	 */
	public void put(String keyStr, String valueStr) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry(stringToBytes(keyStr));
		DatabaseEntry data = new DatabaseEntry(stringToBytes(valueStr));
		db.put(null, key, data);            
	}
    
    /**
     * retrieve the value assoicated with keyStr from persistant storage
     * @param keyStr
     * @return String value associated with key, or null if no key is found
     * or an error occurs
     * @throws DatabaseException 
     */
    public String get(String keyStr) throws DatabaseException {
		String result = null;
		DatabaseEntry key = new DatabaseEntry(stringToBytes(keyStr));
		DatabaseEntry data = new DatabaseEntry();
		if (db.get(null, key, data, LockMode.DEFAULT) 
				== OperationStatus.SUCCESS) {
		
			result = bytesToString(data.getData());
		}
		return result;
    }
    
    /**
     * @param keyStr
     * @throws DatabaseException
     */
    public void delete(String keyStr) throws DatabaseException {
    	db.delete(null,new DatabaseEntry(stringToBytes(keyStr)));
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
}
