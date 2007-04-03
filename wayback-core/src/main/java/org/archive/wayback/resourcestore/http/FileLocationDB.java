/* FileLocationDB
 *
 * $Id$
 *
 * Created on 3:08:59 PM Aug 18, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore.http;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import org.archive.wayback.PropertyConfigurable;
import org.archive.wayback.bdb.BDBRecordSet;
import org.archive.wayback.core.PropertyConfiguration;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.util.CloseableIterator;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileLocationDB extends BDBRecordSet implements PropertyConfigurable {
	private static final Logger LOGGER = Logger.getLogger(
			FileLocationDB.class.getName());

	/**
	 * String id for implementation class of FileLocationDBs.
	 */
	public static final String FILE_LOCATION_DB_CLASS = "filelocationdb";
	
	protected static final String ARC_DB_PATH = "filelocationdb.path";

	protected static final String ARC_DB_NAME = "filelocationdb.name";
	
	protected static final String ARC_DB_LOG = "filelocationdb.logpath";

	private final static String urlDelimiter = " ";
	
	private final static String urlDelimiterRE = " ";

	private FileLocationDBLog log;
	
	/**
	 * Constructor
	 */
	public FileLocationDB() {
		super();
	}
	
	public void init(Properties p) throws ConfigurationException {
		PropertyConfiguration pc = new PropertyConfiguration(p);
		File dbDir = pc.getDir(ARC_DB_PATH, true);
		String dbName = pc.getString(ARC_DB_NAME);
		String logPath = pc.getString(ARC_DB_LOG);
		try {

			LOGGER.info("Initializing FileLocationDB at(" + 
					dbDir.getAbsolutePath() + ") named(" + dbName + ")");
			initializeDB(dbDir.getAbsolutePath(), dbName);
			log = new FileLocationDBLog(logPath);
			
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
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
		String valueString = get(arcName);
		if(valueString != null && valueString.length() > 0) {
			arcUrls = valueString.split(urlDelimiterRE);
		}
		return arcUrls;
	}
	
	/**
	 * add an Url location for an arcName, unless it already exists
	 * @param arcName
	 * @param arcUrl
	 * @throws DatabaseException
	 * @throws IOException 
	 */
	public void addArcUrl(final String arcName, final String arcUrl) throws DatabaseException, IOException {
		
		// need to first see if there is already an entry for this arcName.
		// if not, add arcUrl as the value.
		// if so, check the current arcUrl locations for arcName
		//     if arcUrl exists, do nothing
		//     if arcUrl does not exist, add, and set that as the value.
		
		String newValue = null;
		String oldValue = get(arcName);
		if(oldValue != null && oldValue.length() > 0) {
			String curUrls[] = oldValue.split(urlDelimiterRE);
			boolean found = false;
			for(int i=0; i < curUrls.length; i++) {
				if(arcUrl.equals(curUrls[i])) {
					found = true;
					break;
				}
			}
			if(found == false) {
				newValue = oldValue + " " + arcUrl;
			}
		} else {
			// null or empty value
			newValue = arcUrl;
			if(oldValue == null) log.addArc(arcName);
		}
		
		// did we find a value?
		if(newValue != null) {
			put(arcName,newValue);
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
		
		StringBuilder newValue = new StringBuilder();
		String oldValue = get(arcName);
		if(oldValue != null && oldValue.length() > 0) {
			String curUrls[] = oldValue.split(urlDelimiterRE);

			for(int i=0; i < curUrls.length; i++) {
				if(!arcUrl.equals(curUrls[i])) {
					if(newValue.length() > 0) {
						newValue.append(urlDelimiter);
					}
					newValue.append(curUrls[i]);
				}
			}
			
			if(newValue.length() > 0) {
				
				// update
				put(arcName, newValue.toString());
				
			} else {
				
				// remove the entry:
				delete(arcName);
			}
		}
	}

	/**
	 * @param start
	 * @param end
	 * @return Iterator for traversing arcs between start and end.
	 * @throws IOException
	 */
	public CloseableIterator getArcsBetweenMarks(long start, long end) throws IOException {
		return log.getArcsBetweenMarks(start, end);
	}

	/**
	 * @return current "Mark" for the log. Currently, it's just the length of
	 * 	the log file.
	 */
	public long getCurrentMark() {
		return log.getCurrentMark();
	}
}
