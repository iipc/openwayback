/* BDBResourceIndexWriter
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

package org.archive.wayback.cdx.indexer;

import java.io.File;
import java.io.RandomAccessFile;

import org.archive.wayback.cdx.BDBResourceIndex;
import org.archive.wayback.cdx.CDXRecord;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;

import com.sleepycat.je.DatabaseException;

/**
 * Implements updates to a BDBResourceIndex
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class BDBResourceIndexWriter {
	// TODO: move to somewhere better...
	private final static String CDX_HEADER_MAGIC = " CDX ";
	private BDBResourceIndex db = null;

	/**
	 * Constructor
	 */
	public BDBResourceIndexWriter() {
		super();
	}

	protected void init(final String thePath, final String theDbName)
			throws Exception {
		db = new BDBResourceIndex(thePath, theDbName);
	}

	protected void init(BDBResourceIndex db) {
		this.db = db;
	}

	protected void shutdown() throws DatabaseException {
		db.shutdownDB();
	}

	/**
	 * reads all ResourceResult objects from CDX at filePath, and merges them
	 * into the BDBResourceIndex.
	 * 
	 * @param indexFile
	 *            to CDX file
	 * @throws Exception
	 */
	public void importFile(File indexFile) throws Exception {
		SearchResults results = readFile(indexFile);
		db.addResults(results);
	}

	private SearchResults readFile(File indexFile) throws Exception {
		RandomAccessFile raFile = new RandomAccessFile(indexFile, "r");
		SearchResults results = new SearchResults();
		int lineNumber = 0;
		CDXRecord cdxRecord = new CDXRecord();
		while (true) {
			String line = raFile.readLine();
			if (line == null) {
				break;
			}
			lineNumber++;
			if ((lineNumber == 1) && (line.contains(CDX_HEADER_MAGIC))) {
				continue;
			}
			cdxRecord.parseLine(line, lineNumber);
			SearchResult result = cdxRecord.toSearchResult();
			results.addSearchResult(result);
		}
		return results;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BDBResourceIndexWriter idx = new BDBResourceIndexWriter();
			idx.init(args[0], args[1]);
			idx.importFile(new File(args[2]));

			idx.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
