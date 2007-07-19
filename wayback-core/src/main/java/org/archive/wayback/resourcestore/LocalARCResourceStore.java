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
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpException;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.PropertyConfiguration;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourceindex.indexer.ArcIndexer;
import org.archive.wayback.resourceindex.indexer.IndexClient;

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
	private static final String RESOURCE_PATH = "resourcestore.arcpath";
	private static final String AUTO_INDEX = "resourcestore.autoindex";
	private static final String TMP_PATH = "resourcestore.tmppath";
	private static final String WORK_PATH = "resourcestore.workpath";
	private static final String QUEUED_PATH = "resourcestore.queuedpath";
	private static final String INDEX_TARGET = "resourcestore.indextarget";
	private static final String INDEX_INTERVAL = "resourcestore.indexinterval";

	private File arcDir = null;
	private File tmpDir = null;
	private File workDir = null;
	private File queuedDir = null;
	private String indexTarget = null;
	private int runInterval = DEFAULT_RUN_INTERVAL_MS;
	private IndexClient indexClient = null;
	private ArcIndexer indexer = new ArcIndexer();
	
	/**
	 * Thread object of update thread -- also is flag indicating if the thread
	 * has already been started -- static, and access to it is synchronized.
	 */
	private static Thread indexThread = null;
	
	public void init(Properties p) throws ConfigurationException {
		PropertyConfiguration pc = new PropertyConfiguration(p);
		arcDir = pc.getDir(RESOURCE_PATH, true);
		String autoIndex = p.getProperty(AUTO_INDEX);
		if((autoIndex != null) && (autoIndex.compareTo("1") == 0)) {
			tmpDir = pc.getDir(TMP_PATH,true);
			workDir = pc.getDir(WORK_PATH,true);
			queuedDir = pc.getDir(QUEUED_PATH,true);
			indexTarget = pc.getString(INDEX_TARGET);
			
			if(indexTarget.startsWith("http://")) {
				indexClient = new IndexClient(indexTarget);
			}
			runInterval = pc.getInt(INDEX_INTERVAL,DEFAULT_RUN_INTERVAL_MS);
			startAutoIndexThread();
		}
	}

	/**
	 * @throws ConfigurationException
	 */
	public void init() throws ConfigurationException {
		if(arcDir == null) {
			throw new ConfigurationException("No arcDir set");
		}
		if(indexTarget != null) {
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
			Resource r = new Resource((ARCRecord) rec, reader);
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
	
	private boolean uploadCDX(File cdxFile) {
		boolean uploaded = false;
		if(indexClient == null) {
			// assume we just need to move it to a local directory:
			File toBeMergedDir = new File(indexTarget);
			File toBeMergedFile = new File(toBeMergedDir,cdxFile.getName());
			if(toBeMergedFile.exists()) {
				LOGGER.severe("WARNING: "+toBeMergedFile.getAbsolutePath() +
						"already exists!");
			} else {
				if(cdxFile.renameTo(toBeMergedFile)) {
					LOGGER.info("Queued " + toBeMergedFile.getAbsolutePath() + 
							" for merging.");
					uploaded = true;
				} else {
					LOGGER.severe("FAILED rename("+cdxFile.getAbsolutePath()+
							") to ("+toBeMergedFile.getAbsolutePath()+")");
				}
			}
		} else {
			// use indexClient to upload:
			try {
				indexClient.uploadCDX(cdxFile);
				LOGGER.info("Uploaded " + cdxFile.getAbsolutePath());
				uploaded = true;
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return uploaded;
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
				File cdxFile = new File(tmpDir,base);

				try {
					LOGGER.info("Indexing ARC " + arcFile.getAbsolutePath());
					SearchResults res = indexer.indexArc(arcFile);
					LOGGER.info("Serializing ARC data in " + 
							cdxFile.getAbsolutePath());
					indexer.serializeResults(res, cdxFile);
					if(uploadCDX(cdxFile)) {
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
	
	// TODO: refactor to single location
	private File ensureDir(String path) throws ConfigurationException {
		if(path.length() < 1) {
			throw new ConfigurationException("Empty directory path");
		}
		File dir = new File(path);
		if(dir.exists()) {
			if(!dir.isDirectory()) {
				throw new ConfigurationException("path " + path + "exists" +
						"but is not a directory");
			}
		} else {
			if(!dir.mkdirs()) {
				throw new ConfigurationException("unable to create directory" +
						" at " + path);
			}
		}
		return dir;
	}


	/**
	 * @return String path to tmpDir
	 */
	public String getTmpDir() {
		if(tmpDir == null) {
			return null;
		}
		return tmpDir.getAbsolutePath();
	}
	/**
	 * @param tmpDir the tmpDir to set
	 * @throws ConfigurationException 
	 */
	public void setTmpDir(String tmpDir) throws ConfigurationException {
		this.tmpDir = ensureDir(tmpDir);
	}

	/**
	 * @return String path to workDir
	 */
	public String getWorkDir() {
		if(workDir == null) {
			return null;
		}
		return workDir.getAbsolutePath();
	}
	/**
	 * @param workDir the workDir to set
	 * @throws ConfigurationException 
	 */
	public void setWorkDir(String workDir) throws ConfigurationException {
		this.workDir = ensureDir(workDir);
	}

	/**
	 * @return String path to queuedDir
	 */
	public String getQueuedDir() {
		if(queuedDir == null) {
			return null;
		}
		return queuedDir.getAbsolutePath();
	}
	/**
	 * @param queuedDir the queuedDir to set
	 * @throws ConfigurationException 
	 */
	public void setQueuedDir(String queuedDir) throws ConfigurationException {
		this.queuedDir = ensureDir(queuedDir);
	}

	/**
	 * @return
	 */
	public String getIndexTarget() {
		return indexTarget;
	}
	/**
	 * @param indexTarget the indexTarget to set
	 */
	public void setIndexTarget(String indexTarget) {
		this.indexTarget = indexTarget;
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
		if(arcDir == null) {
			return null;
		}
		return arcDir.getAbsolutePath();
	}
	/**
	 * @param arcDir the arcDir to set
	 */
	public void setArcDir(String arcDir) {
		this.arcDir = new File(arcDir);
	}
}
