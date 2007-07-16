/* BDBIndex
 *
 * $Id$
 *
 * Created on 4:48:46 PM Aug 17, 2006.
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
package org.archive.wayback.resourceindex.bdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.bdb.BDBRecordSet;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.SearchResultSource;
import org.archive.wayback.resourceindex.indexer.ArcIndexer;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.CloseableIterator;

import com.sleepycat.je.DatabaseException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBIndex extends BDBRecordSet implements SearchResultSource {
	private String bdbPath = null;
	private String bdbName = null;
	private BDBIndexUpdater updater = null;
	/**
	 * @throws DatabaseException
	 * @throws ConfigurationException
	 */
	public void init() throws DatabaseException, ConfigurationException {
		initializeDB(bdbPath,bdbName);
		if(updater != null) {
			updater.setIndex(this);
			updater.startup();
		}
	}

	private CloseableIterator adaptIterator(Iterator itr) {
		return new AdaptedIterator(itr,new BDBRecordToSearchResultAdapter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator getPrefixIterator(String prefix)
			throws ResourceIndexNotAvailableException {
		
		try {
			return adaptIterator(recordIterator(prefix,true));
		} catch (DatabaseException e) {
			throw new ResourceIndexNotAvailableException(e.getMessage()); 
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixReverseIterator(java.lang.String)
	 */
	public CloseableIterator getPrefixReverseIterator(String prefix)
			throws ResourceIndexNotAvailableException {
		try {
			return adaptIterator(recordIterator(prefix,false));
		} catch (DatabaseException e) {
			throw new ResourceIndexNotAvailableException(e.getMessage()); 
		}
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#cleanup(org.archive.wayback.util.CleanableIterator)
	 */
	public void cleanup(CloseableIterator c) throws IOException {
		c.close();
	}
	
	private static void USAGE() {
		System.err.println("Usage: DBPATH DBNAME -w");
		System.err.println("\tRead lines from STDIN, inserting into BDBJE at\n" +
				" DBPATH named DBNAME, creating DB if needed.");

		System.err.println("Usage: DBPATH DBNAME -r [PREFIX]");
		System.err.println("\tDump lines from BDBJE at path DBPATH named " +
				"DBNAME\n to STDOUT. If PREFIX is specified, only output " +
				"records\n beginning with PREFIX, otherwise output all " +
				"records");
		
		System.exit(3);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length < 3) {
			USAGE();
		}
		String path = args[0];
		String name = args[1];
		String op = args[2];
		BDBIndex index = new BDBIndex();
		int BATCH_SIZE = 1000;
		ArcIndexer indexer = new ArcIndexer();

		try {
			index.initializeDB(path,name);
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if(op.compareTo("-r") == 0) {
			PrintWriter pw = new PrintWriter(System.out);
			CaptureSearchResults results = new CaptureSearchResults();
			if(args.length == 4) {
				String prefix = args[3];
				CloseableIterator itr = null;
				try {
					itr = index.getPrefixIterator(prefix);
				} catch (ResourceIndexNotAvailableException e) {
					e.printStackTrace();
					System.exit(1);
				}
				while(itr.hasNext()) {
					SearchResult result = (SearchResult) itr.next();
					String urlS = result.get(WaybackConstants.RESULT_URL_KEY);
					if(!urlS.startsWith(prefix)) {
						break;
					}
					results.addSearchResult(result);
					if(results.getResultCount() > BATCH_SIZE) {
						try {
							indexer.serializeResults(results,pw,false);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(2);
						}
						results = new CaptureSearchResults();
					}
				}
				if(results.getResultCount() > 0) {
					try {
						indexer.serializeResults(results,pw,false);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(2);
					}
				}
			} else {
				CloseableIterator itr = null;
				try {
					itr = index.getPrefixIterator(" ");
				} catch (ResourceIndexNotAvailableException e) {
					e.printStackTrace();
					System.exit(1);
				}
				while(itr.hasNext()) {
					SearchResult result = (SearchResult) itr.next();
					results.addSearchResult(result);
					if(results.getResultCount() > BATCH_SIZE) {
						try {
							indexer.serializeResults(results,pw,false);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(2);
						}
						results = new CaptureSearchResults();
					}
				}
				if(results.getResultCount() > 0) {
					try {
						indexer.serializeResults(results,pw,false);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(2);
					}
				}
				pw.flush();
				pw.close();
				
			}
			
		} else if(op.compareTo("-w") == 0) {
			File tmpCDX = null;
			int total = 0;
			int numInTmp = 0;
			try {
				tmpCDX = File.createTempFile("reader",".cdx");
				PrintWriter pw = new PrintWriter(tmpCDX);
				// need to break the results from STDIN into chunks -- each chunk
				// is written to a file, then added to the index.
				BufferedReader br = new BufferedReader(
						new InputStreamReader(System.in));
				
				while(true) {
					String line = br.readLine();
					if(line == null) {
						break;
					}
					pw.println(line);
					numInTmp++;
					total++;
					if(numInTmp > BATCH_SIZE) {
						pw.flush();
						pw.close();
						index.insertRecords(
								indexer.getCDXFileBDBRecordIterator(tmpCDX));
						System.err.println("Wrote " + numInTmp + " to index..");
						pw = new PrintWriter(tmpCDX);
						numInTmp = 0;
					}
				}
				if(numInTmp > 0) {
					pw.flush();
					pw.close();
					index.insertRecords(
							indexer.getCDXFileBDBRecordIterator(tmpCDX));					
					System.err.println("Wrote last " + numInTmp + " to index.");
				}
				tmpCDX.delete();
				System.out.println("Total of " + total + " docs inserted.");
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			USAGE();
		}
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

	/**
	 * @return the updater
	 */
	public BDBIndexUpdater getUpdater() {
		return updater;
	}

	/**
	 * @param updater the updater to set
	 */
	public void setUpdater(BDBIndexUpdater updater) {
		this.updater = updater;
	}
}
