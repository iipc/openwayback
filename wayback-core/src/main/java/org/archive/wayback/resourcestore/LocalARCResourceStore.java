/* LocalARCResourceStore
 *
 * $Id$
 *
 * Created on 5:14:45 PM Oct 12, 2006.
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
package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

//import org.apache.commons.httpclient.HttpException;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
//import org.archive.wayback.core.SearchResults;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourceindex.indexer.IndexClient;
//import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.DirMaker;

/**
 * Implements ResourceStore using a local directory of ARC files.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LocalARCResourceStore implements ResourceStore {
	private static final Logger LOGGER =
        Logger.getLogger(LocalARCResourceStore.class.getName());

	private final static int DEFAULT_RUN_INTERVAL_MS = 10000;

	private File arcDir = null;
	private File workDir = null;
	private File queuedDir = null;
	private int runInterval = DEFAULT_RUN_INTERVAL_MS;
	private IndexClient indexClient = null;
	private ArcIndexer indexer = new ArcIndexer();
	
	/**
	 * Thread object of update thread -- also is flag indicating if the thread
	 * has already been started -- static, and access to it is synchronized.
	 */
	private Thread indexThread = null;
	
	/**
	 * @throws ConfigurationException
	 */
	public void init() throws ConfigurationException {
		if(arcDir == null) {
			throw new ConfigurationException("No arcDir set");
		}
		if(indexClient != null) {
			startAutoIndexThread();
		}
	}
	
	public Resource retrieveResource(SearchResult result) throws IOException, 
		ResourceNotAvailableException {

		String arcName = resultToARCName(result);
		long offset = resultToARCOffset(result);
		if (!arcName.endsWith(ARCReader.DOT_COMPRESSED_ARC_FILE_EXTENSION)) {
			arcName += ARCReader.DOT_COMPRESSED_ARC_FILE_EXTENSION;
		}
		File arcFile = new File(arcName);
		if (!arcFile.isAbsolute()) {
			arcFile = new File(arcDir, arcName);
		}
		if (!arcFile.exists() || !arcFile.canRead()) {
			
			// TODO: this needs to be prettied up for end user consumption..
			throw new ResourceNotAvailableException("Cannot find ARC file ("
					+ arcFile.getAbsolutePath() + ")");
		} else {

			ARCReader reader = ARCReaderFactory.get(arcFile);

			ArchiveRecord rec = reader.get(offset);
			// TODO: handle other types of ArchiveRecords...
			if(!(rec instanceof ARCRecord)) {
				throw new ResourceNotAvailableException("Bad ARCRecord format");
			}
			Resource r = new ArcResource((ARCRecord) rec, reader);
			return r;
		}
	}

	protected String resultToARCName(SearchResult result) {
		return result.get(WaybackConstants.RESULT_ARC_FILE);
	}

	protected long resultToARCOffset(SearchResult result) {
		return Long.parseLong(result.get(WaybackConstants.RESULT_OFFSET));
	}

	/**
	 * Find any new ARC files and queue them for indexing. 
	 * @throws IOException
	 */
	public void queueNewArcsForIndex() throws IOException {
		
		// build a HashMap of what has been queued already:
		HashMap<String,String> queued = new HashMap<String, String>();
		String entries[] = queuedDir.list();
		if(entries != null) {
			for (int i = 0; i < entries.length; i++) {
				queued.put(entries[i], "i");
			}
		}
		// now scan thru arcDir, and make a flag file for anything that was not
		// already there:
		String arcs[] = arcDir.list();
		if (arcs != null) {
			for (int i = 0; i < arcs.length; i++) {
				if (arcs[i].endsWith(".arc.gz") && 
						!queued.containsKey(arcs[i])) {
					
					File arc = new File(arcDir,arcs[i]);
					if(arc.isFile()) {
						
						File newQueuedFile = new File(queuedDir,arcs[i]);
						File newToBeIndexedFile = new File(workDir,arcs[i]);
						newToBeIndexedFile.createNewFile();
						newQueuedFile.createNewFile();
					}
				}
			}
		}
	}
	
	/**
	 * Index up to 'max' ARC files queued for indexing, queueing the resulting 
	 * CDX files for merging with the BDBIndex.
	 * 
	 * @param indexer
	 * @param max maximum number to index in this method call, 0 for unlimited
	 * @return int number of ARC files indexed
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public int indexArcs(ArcIndexer indexer, int max) 
	throws MalformedURLException, IOException {

		int numIndexed = 0;
		String toBeIndexed[] = workDir.list();
		
		if (toBeIndexed != null) {
			for (int i = 0; i < toBeIndexed.length; i++) {
				String base = toBeIndexed[i];
				File arcFile = new File(arcDir,base);
				File workFlagFile = new File(workDir,base);
				String cdxBase;
				if(base.endsWith(".arc.gz")) {
					cdxBase = base.substring(0,base.length() - 7);
				} else if(base.endsWith(".arc")) {
					cdxBase = base.substring(0,base.length() - 4);
				} else {
					cdxBase = base;
				}

				try {
					
					LOGGER.info("Indexing ARC " + arcFile.getAbsolutePath());
					Iterator<SearchResult> itr = indexer.iterator(arcFile);
					
					if(indexClient.addSearchResults(cdxBase, itr)) {
						if (!workFlagFile.delete()) {
							throw new IOException("Unable to delete "
									+ workFlagFile.getAbsolutePath());
						}
					}
					numIndexed++;
				} catch (IOException e) {
					LOGGER.severe("FAILED index of " + arcFile.getAbsolutePath() +
							" cause: " + e.getLocalizedMessage());
				}
				if(max > 0 && (numIndexed >= max)) {
					break;
				}
			}
		}
		return numIndexed;
	}
	
	/**
	 * Scan for new ARC files, and index any new files discovered.
	 * 
	 * There are 3 main steps, which could be broken into separate threads:
	 * 1) detect new ARCs
	 * 2) create CDX files for each new ARC
	 * 3) upload CDX files to target (or rename to local "incoming" directory)
	 * 
	 * for now these are sequential.
	 * 
	 * @return number of ARC files indexed
	 */
	public int indexNewArcs() {
		int numIndexed = 0;
		try {
			queueNewArcsForIndex();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			numIndexed = indexArcs(indexer,10);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return numIndexed;
	}
	
	/**
	 * start the AutoARCIndexThread thread, which will scan for new arcs, index
	 * new arcs that appear, and merge indexed arcs (in CDX format) into the
	 * BDBResourceIndex
	 */
	private synchronized void startAutoIndexThread() {
		if (indexThread != null) {
			return;
		}
		indexThread = new AutoARCIndexThread(this,runInterval);
		indexThread.start();
	}

	/**
	 * Thread that repeatedly calls indexNewArcs on the LocalARCResourceStore.
	 */
	private class AutoARCIndexThread extends Thread {
		
		private LocalARCResourceStore store = null;

		private int runInterval;

		/**
		 * @param store
		 * @param runInterval
		 */
		public AutoARCIndexThread(LocalARCResourceStore store,
				int runInterval) {
			super("AutoARCIndexThread");
			super.setDaemon(true);
			this.store = store;
			this.runInterval = runInterval;
			LOGGER.info("AutoARCIndexThread is alive.");
		}

		public void run() {
			int sleepInterval = runInterval;
			while (true) {
				try {
					int numIndexed = store.indexNewArcs();
					if (numIndexed == 0) {
						sleep(sleepInterval);
						sleepInterval += runInterval;
					} else {
						sleepInterval = runInterval;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return String path to workDir
	 */
	public String getWorkDir() {
		return DirMaker.getAbsolutePath(workDir);
	}
	/**
	 * @param workDir the workDir to set
	 * @throws IOException 
	 */
	public void setWorkDir(String workDir) throws IOException {
		this.workDir = DirMaker.ensureDir(workDir);
	}

	/**
	 * @return String path to queuedDir
	 */
	public String getQueuedDir() {
		return DirMaker.getAbsolutePath(queuedDir);
	}
	/**
	 * @param queuedDir the queuedDir to set
	 * @throws IOException 
	 */
	public void setQueuedDir(String queuedDir) throws IOException {
		this.queuedDir = DirMaker.ensureDir(queuedDir);
	}

	/**
	 * @return integer milliseconds between polls for new ARC content.
	 */
	public int getRunInterval() {
		return runInterval;
	}
	/**
	 * @param runInterval the runInterval to set
	 */
	public void setRunInterval(int runInterval) {
		this.runInterval = runInterval;
	}
	/**
	 * @return the arcDir
	 */
	public String getArcDir() {
		return DirMaker.getAbsolutePath(arcDir);
	}
	/**
	 * @param arcDir the arcDir to set
	 * @throws IOException 
	 */
	public void setArcDir(String arcDir) throws IOException {
		this.arcDir = DirMaker.ensureDir(arcDir);
	}

	/**
	 * @return the indexClient
	 */
	public IndexClient getIndexClient() {
		return indexClient;
	}

	/**
	 * @param indexClient the indexClient to set
	 */
	public void setIndexClient(IndexClient indexClient) {
		this.indexClient = indexClient;
	}

	public void shutdown() throws IOException {
		// no-op... could shutdown threads...
	}
}
