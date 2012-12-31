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
package org.archive.wayback.resourceindex.bdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.UpdatableSearchResultSource;
import org.archive.wayback.resourceindex.cdx.CDXLineToSearchResultAdapter;
import org.archive.wayback.resourceindex.cdx.SearchResultToCDXLineAdapter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.ByteOp;
import org.archive.wayback.util.bdb.BDBRecord;
import org.archive.wayback.util.bdb.BDBRecordSet;
import org.archive.wayback.util.flatfile.RecordIterator;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

import com.sleepycat.je.DatabaseException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBIndex extends BDBRecordSet implements 
		UpdatableSearchResultSource {
	
	private String bdbPath = null;
	private String bdbName = null;
	/**
	 * @throws DatabaseException
	 * @throws ConfigurationException
	 */
	public void init() throws IOException, ConfigurationException {
		initializeDB(bdbPath,bdbName);
	}

	private CloseableIterator<CaptureSearchResult> adaptIterator(
			Iterator<BDBRecord> itr) {
		return new AdaptedIterator<BDBRecord,CaptureSearchResult>(itr,
				new BDBRecordToSearchResultAdapter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator<CaptureSearchResult> getPrefixIterator(String prefix)
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
	public CloseableIterator<CaptureSearchResult> getPrefixReverseIterator(String prefix)
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
	public void cleanup(CloseableIterator<CaptureSearchResult> c) throws IOException {
		c.close();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.UpdatableSearchResultSource#addSearchResults(java.util.Iterator)
	 */
	public void addSearchResults(Iterator<CaptureSearchResult> itr, 
			UrlCanonicalizer canonicalizer) throws IOException {
		Adapter<CaptureSearchResult,BDBRecord> adapterSRtoBDB = 
			new SearchResultToBDBRecordAdapter(canonicalizer);

		Iterator<BDBRecord> itrBDB =
			new AdaptedIterator<CaptureSearchResult,BDBRecord>(itr,
					adapterSRtoBDB);

		insertRecords(itrBDB);
		
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
		UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();
		try {
			index.initializeDB(path,name);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if(op.compareTo("-r") == 0) {
			PrintWriter pw = new PrintWriter(System.out);

			CloseableIterator<CaptureSearchResult> itrSR = null;
			Adapter<CaptureSearchResult,String> adapter = 
				new SearchResultToCDXLineAdapter();
			CloseableIterator<String> itrS;

			if(args.length == 4) {
				String prefix = args[3];
				try {
					itrSR = index.getPrefixIterator(prefix);
				} catch (ResourceIndexNotAvailableException e) {
					e.printStackTrace();
					System.exit(1);
				}
				itrS = new AdaptedIterator<CaptureSearchResult,String>(itrSR,adapter);
				while(itrS.hasNext()) {
					String line = itrS.next();
					if(!line.startsWith(prefix)) {
						break;
					}
					pw.println(line);
				}
		
			} else {
				try {
					itrSR = index.getPrefixIterator(" ");
				} catch (ResourceIndexNotAvailableException e) {
					e.printStackTrace();
					System.exit(1);
				}
				itrS = new AdaptedIterator<CaptureSearchResult,String>(itrSR,adapter);

				while(itrS.hasNext()) {
					pw.println(itrS.next());
				}
			}

			try {
				itrS.close();
				itrSR.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(2);
			}
			pw.flush();
			pw.close();
			
		} else if(op.compareTo("-w") == 0) {

			BufferedReader br = new BufferedReader(
					new InputStreamReader(System.in,ByteOp.UTF8));
			
			RecordIterator itrS = new RecordIterator(br);

			Adapter<String,CaptureSearchResult> adapterStoSR = 
				new CDXLineToSearchResultAdapter();
			
			Iterator<CaptureSearchResult> itrSR = 
				new AdaptedIterator<String,CaptureSearchResult>(itrS,adapterStoSR);
			
			try {
				index.addSearchResults(itrSR, canonicalizer);
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

	public void shutdown() throws IOException {
		try {
			shutdownDB();
		} catch (DatabaseException e) {
			throw new IOException(e.getMessage());
		}
	}
}
