/* LocalBDBResourceIndex
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

import java.io.IOException;
import java.util.Properties;

import org.archive.wayback.ResourceIndex;
import org.archive.wayback.arcindexer.IndexPipeline;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.WaybackException;

/**
 * Implements ResourceIndex interface using a BDBResourceIndex
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class LocalBDBResourceIndex implements ResourceIndex {

	private final static String INDEX_PATH = "resourceindex.indexpath";

	private final static String DB_NAME = "resourceindex.dbname";

	private final static int MAX_RECORDS = 1000;

	private BDBResourceIndex db = null;
	
	private IndexPipeline pipeline = null;

	/**
	 * Constructor
	 */
	public LocalBDBResourceIndex() {
		super();
	}

	public void init(Properties p) throws Exception {
		System.out.println("initializing LocalDBDResourceIndex...");
		String dbPath = (String) p.get(INDEX_PATH);
		if (dbPath == null || (dbPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + INDEX_PATH);
		}
		String dbName = (String) p.get(DB_NAME);
		if (dbName == null || (dbName.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + DB_NAME);
		}
		db = new BDBResourceIndex(dbPath,dbName);
		pipeline = new IndexPipeline();
		pipeline.init(p);
	}

	public ResourceResults query(WMRequest request) throws IOException,
			WaybackException {
		String searchHost = request.getRequestURI().getHostBasename();
		String searchPath = request.getRequestURI().getEscapedPathQuery();

		String searchUrl = searchHost + searchPath;

		if (request.isRetrieval()) {
			return db.doUrlSearch(searchUrl, request.getStartTimestamp()
					.getDateStr(), request.getEndTimestamp().getDateStr(),
					null, MAX_RECORDS);
		} else if (request.isQuery()) {
			return db.doUrlSearch(searchUrl, request.getStartTimestamp()
					.getDateStr(), request.getEndTimestamp().getDateStr(),
					null, MAX_RECORDS);
		} else if (request.isPathQuery()) {
			return db.doUrlPrefixSearch(searchUrl, request.getStartTimestamp()
					.getDateStr(), request.getEndTimestamp().getDateStr(),
					null, MAX_RECORDS);
		} else {
			throw new BadQueryException("Unknown query type");
		}
	}
}
