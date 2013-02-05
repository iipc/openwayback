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
package org.archive.wayback.resourcestore.locationdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.util.ByteOp;
import org.archive.wayback.util.bdb.BDBRecordSet;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBResourceFileLocationDB implements ResourceFileLocationDB {

	private final static String urlDelimiter = " ";
	
	private final static String urlDelimiterRE = " ";

	private ResourceFileLocationDBLog log;
	private BDBRecordSet bdb = null;
	private String logPath = null;
	private String bdbPath = null;
	private String bdbName = null;
	
	private IOException wrapDBException(DatabaseException e) {
		return new IOException(e.getLocalizedMessage());
	}
	private String get(String key) throws IOException {
		try {
			return bdb.get(key);
		} catch (DatabaseException e) {
			throw wrapDBException(e);
		}
	}
	private void put(String key, String value) throws IOException {
		try {
			bdb.put(key,value);
		} catch (DatabaseException e) {
			throw wrapDBException(e);
		}
	}
	private void delete(String key) throws IOException {
		try {
			bdb.delete(key);
		} catch (DatabaseException e) {
			throw wrapDBException(e);
		}
	}
	public void shutdown() throws IOException {
		try {
			bdb.shutdownDB();
		} catch (DatabaseException e) {
			throw wrapDBException(e);
		}
	}
	
	public void init() throws IOException {
		bdb = new BDBRecordSet();
		bdb.initializeDB(bdbPath,bdbName);
		if(logPath == null) {
			throw new IOException("No logPath");
		}
		log = new ResourceFileLocationDBLog(logPath);
	}

	/**
	 * return an array of String URLs for all known locations of name in the DB.
	 * @param name
	 * @return String[] of URLs to name
	 * @throws IOException
	 */
	public String[] nameToUrls(final String name) throws IOException {
		
		String[] urls = null;
		String valueString = get(name);
		if(valueString != null && valueString.length() > 0) {
			urls = valueString.split(urlDelimiterRE);
		}
		return urls;
	}
	
	/**
	 * add an url location for a name, unless it already exists
	 * @param name
	 * @param url
	 * @throws IOException 
	 */
	public void addNameUrl(final String name, final String url) 
	throws IOException {
		
		// need to first see if there is already an entry for this name.
		// if not, add url as the value.
		// if so, check the current url locations for name
		//     if url exists, do nothing
		//     if url does not exist, add, and set that as the value.
		
		String newValue = null;
		String oldValue = get(name);
		if(oldValue != null && oldValue.length() > 0) {
			String curUrls[] = oldValue.split(urlDelimiterRE);
			boolean found = false;
			for(int i=0; i < curUrls.length; i++) {
				if(url.equals(curUrls[i])) {
					found = true;
					break;
				}
			}
			if(found == false) {
				newValue = oldValue + " " + url;
			}
		} else {
			// null or empty value
			newValue = url;
			if(oldValue == null) log.addName(name);
		}
		
		// did we find a value?
		if(newValue != null) {
			put(name,newValue);
		}
	}

	/**
	 * remove a single url location for an name, if it exists
	 * @param name
	 * @param url
	 * @throws IOException
	 */
	public void removeNameUrl(final String name, final String url) 
	throws IOException {
		// need to first see if there is already an entry for this name.
		// if not, do nothing
		// if so, loop thru all current url locations for name
		//     keep any that are not url
		// if any locations are left, update to the new value, sans url
		// if none are left, remove the entry from the db
		
		StringBuilder newValue = new StringBuilder();
		String oldValue = get(name);
		if(oldValue != null && oldValue.length() > 0) {
			String curUrls[] = oldValue.split(urlDelimiterRE);

			for(int i=0; i < curUrls.length; i++) {
				if(!url.equals(curUrls[i])) {
					if(newValue.length() > 0) {
						newValue.append(urlDelimiter);
					}
					newValue.append(curUrls[i]);
				}
			}
			
			if(newValue.length() > 0) {
				
				// update
				put(name, newValue.toString());
				
			} else {
				
				// remove the entry:
				delete(name);
			}
		}
	}

	/**
	 * @param start
	 * @param end
	 * @return Iterator for traversing arcs between start and end.
	 * @throws IOException
	 */
	public CloseableIterator<String> getNamesBetweenMarks(long start, long end) 
	throws IOException {
		return log.getNamesBetweenMarks(start, end);
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
		BDBResourceFileLocationDB db = new BDBResourceFileLocationDB();
		db.setBdbPath(bdbPath);
		db.setBdbName(bdbName);
		db.setLogPath(logPath);
		BufferedReader r = new BufferedReader(
				new InputStreamReader(System.in,ByteOp.UTF8));
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
				db.addNameUrl(parts[0],parts[1]);
				System.out.println("Added\t" + parts[0] + "\t" + parts[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
			exitCode = 1;
		} finally {
			try {
				db.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
				exitCode = 1;
			}
		}
		System.exit(exitCode);
	}	
}
