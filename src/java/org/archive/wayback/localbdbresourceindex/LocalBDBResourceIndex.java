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

import java.io.File;
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
	private static Thread indexUpdateThread = null;

	private final static String INDEX_PATH = "resourceindex.indexPath";

	private final static String DB_NAME = "resourceindex.dbName";

	private final static String ARC_PATH = "resourceindex.arcPath";

	private final static String WORK_PATH = "resourceindex.workPath";

	private final static String RUN_PIPELINE = "resourceindex.runPipeline";

	private final static int MAX_RECORDS = 1000;

	private BDBResourceIndex db = null;

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
		String arcPath = (String) p.get(ARC_PATH);
		if (arcPath == null || (arcPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + ARC_PATH);
		}

		String workPath = (String) p.get(WORK_PATH);
		if (workPath == null || (workPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + WORK_PATH);
		}
		String dbName = (String) p.get(DB_NAME);
		if (dbName == null || (dbName.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + DB_NAME);
		}
		String runPipeline = (String) p.get(RUN_PIPELINE);
		db = new BDBResourceIndex(dbPath, dbName);
		if (runPipeline != null) {

			System.out
					.println("LocalDBDResourceIndex starting pipeline thread...");
			if (indexUpdateThread == null) {
				IndexPipeline pipeline = new IndexPipeline();
				String mergeDir = workPath + "/mergey";
				pipeline.init(arcPath, mergeDir, workPath);
				startIndexUpdateThead(db, pipeline);
			}
		}
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

	protected synchronized void startIndexUpdateThead(
			final BDBResourceIndex bdb, IndexPipeline pipeline) {
		if (indexUpdateThread != null) {
			return;
		}
		indexUpdateThread = new IndexUpdateThread(bdb, pipeline);
		indexUpdateThread.start();
	}

	/**
	 * Thread that repeatedly runs processing of an IndexPipeline and merges new
	 * data into a BDBResourceIndex
	 * 
	 * @author Brad Tofel
	 * @version $Date$, $Revision$
	 */
	private class IndexUpdateThread extends Thread {
		private final static int SLEEP_MILLISECONDS = 10000;

		BDBResourceIndexWriter importer = null;

		IndexPipeline pipeline = null;

		/**
		 * Constructor
		 * 
		 * @param bdb
		 *            initialized BDBResourceIndex
		 * @param pipeline
		 *            initialized IndexPipeline
		 */
		public IndexUpdateThread(final BDBResourceIndex bdb,
				IndexPipeline pipeline) {
			super("IndexUpdateThread");
			super.setDaemon(true);
			this.importer = new BDBResourceIndexWriter();
			importer.init(bdb);
			this.pipeline = pipeline;
		}

		public void run() {

			while (true) {
				try {
					indexArcs();
					mergeIndex();
					sleep(SLEEP_MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// System.out.println("I'm running!");
			}
		}

		private void indexArcs() {
			try {
				pipeline.indexArcs();
				// System.out.println("Indexed...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void mergeIndex() {
			int numMerged = 0;
			String newFiles[] = pipeline.getMergeDir().list();
			for (int i = 0; i < newFiles.length; i++) {
				// TODO: Special handling of encoding and date.
				File newFile = new File(pipeline.getMergeDir()
						.getAbsolutePath()
						+ "/" + newFiles[i]);

				if (newFile.isFile()) {
					try {
						importer.importFile(newFile.getAbsolutePath());
						if (!newFile.delete()) {
							throw new IOException("Unable to unlink "
									+ newFile.getAbsolutePath());
						}
						numMerged++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (numMerged > 0) {
				System.out.println("Merged " + numMerged + " files.");
			}
		}
	}
}
