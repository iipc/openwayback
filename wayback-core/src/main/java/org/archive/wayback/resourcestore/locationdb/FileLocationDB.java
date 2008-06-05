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
package org.archive.wayback.resourcestore.locationdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.archive.wayback.bdb.BDBRecordSet;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.util.CloseableIterator;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileLocationDB extends BDBRecordSet {

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
	private String logPath = null;
	private String bdbPath = null;
	private String bdbName = null;
	
	/**
	 * Constructor
	 */
	public FileLocationDB() {
		super();
	}
	
	/**
	 * @throws DatabaseException
	 * @throws ConfigurationException
	 */
	public void init() throws DatabaseException, ConfigurationException {
		if(logPath == null) {
			throw new ConfigurationException("No logPath");
		}
		log = new FileLocationDBLog(logPath);
		initializeDB(bdbPath,bdbName);		
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
	public CloseableIterator<String> getArcsBetweenMarks(long start, long end) 
	throws IOException {
		return log.getArcsBetweenMarks(start, end);
	}

	/**
	 * @return current "Mark" for the log. Currently, it's just the length of
	 * 	the log file.
	 */
	public long getCurrentMark() {
		return log.getCurrentMark();
	}

	/**
	 * @return the logPath
	 */
	public String getLogPath() {
		return logPath;
	}

	/**
	 * @param logPath the logPath to set
	 */
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	/**
	 * @return the bdbPath
	 */
	public String getBdbPath() {
		return bdbPath;
	}

	/**
	 * @param bdbPath the bdbPath to set
	 */
	public void setBdbPath(String bdbPath) {
		this.bdbPath = bdbPath;
	}

	/**
	 * @return the bdbName
	 */
	public String getBdbName() {
		return bdbName;
	}

	/**
	 * @param bdbName the bdbName to set
	 */
	public void setBdbName(String bdbName) {
		this.bdbName = bdbName;
	}
	private static void USAGE(String message) {
		System.err.print("USAGE: " + message + "\n" +
				"\tDBDIR DBNAME LOGPATH\n" +
				"\n" +
				"\t\tread lines from STDIN formatted like:\n" +
				"\t\t\tNAME<SPACE>URL\n" +
				"\t\tand for each line, add to locationDB that file NAME is\n" +
				"\t\tlocated at URL. Use locationDB in DBDIR at DBNAME, \n" + 
				"\t\tcreating if it does not exist.\n"
				);
		System.exit(2);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 3) {
			USAGE("");
			System.exit(1);
		}
		String bdbPath = args[0];
		String bdbName = args[1];
		String logPath = args[2];
		FileLocationDB db = new FileLocationDB();
		db.setBdbPath(bdbPath);
		db.setBdbName(bdbName);
		db.setLogPath(logPath);
		BufferedReader r = new BufferedReader(
				new InputStreamReader(System.in));
		String line;
		int exitCode = 0;
		try {
			db.init();
			while((line = r.readLine()) != null) {
				String parts[] = line.split(" ");
				if(parts.length != 2) {
					System.err.println("Bad input(" + line + ")");
					System.exit(2);
				}
				db.addArcUrl(parts[0],parts[1]);
				System.out.println("Added\t" + parts[0] + "\t" + parts[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
			exitCode = 1;
		} catch (DatabaseException e) {
			e.printStackTrace();
			exitCode = 1;
		} catch (ConfigurationException e) {
			e.printStackTrace();
			exitCode = 1;
		} finally {
			try {
				db.shutdownDB();
			} catch (DatabaseException e) {
				e.printStackTrace();
				exitCode = 1;
			}
		}
		System.exit(exitCode);
	}	
}
