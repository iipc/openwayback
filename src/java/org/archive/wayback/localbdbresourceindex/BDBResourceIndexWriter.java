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

package org.archive.wayback.localbdbresourceindex;

import java.io.File;
import java.io.RandomAccessFile;

import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;

import com.sleepycat.je.DatabaseException;

/**
 * Implements updates to a BDBResourceIndex
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class BDBResourceIndexWriter {
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
	 * @param filePath
	 *            to CDX file
	 * @throws Exception
	 */
	public void importFile(String filePath) throws Exception {
		ResourceResults results = readFile(filePath);
		db.addResults(results);
	}

	private ResourceResults readFile(String filePath) throws Exception {
		File file = new File(filePath);
		RandomAccessFile raFile = new RandomAccessFile(file, "r");
		ResourceResults results = new ResourceResults();
		int lineNumber = 0;
		while (true) {
			String line = raFile.readLine();
			if (line == null) {
				break;
			}
			lineNumber++;
			if ((lineNumber == 1) && (line.contains(" CDX "))) {
				continue;
			}
			ResourceResult result = new ResourceResult();
			result.parseLine(line, lineNumber);

			results.addResourceResult(result);
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
			idx.importFile(args[2]);

			idx.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
