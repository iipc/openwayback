/* BDBIndexUpdater
 *
 * $Id$
 *
 * Created on 2:59:40 PM Oct 12, 2006.
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.resourceindex.indexer.ArcIndexer;

/**
 * Class which starts a background thread that repeatedly scans an incoming
 * directory and merges files found therein(which are assumed to be in CDX
 * format) with a BDBIndex. Optional configurations include:
 * 
 *    target directory where merged files are moved to (otherwise deleted)
 *    target directory where failed failed are moved(otherwise left in place)
 *    milliseconds between scans of the incoming directory(default 10000)
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBIndexUpdater {
	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER =
	        Logger.getLogger(BDBIndexUpdater.class.getName());

	private final static int DEFAULT_RUN_INTERVAL_MS = 10000;

	private BDBIndex index = null;

	private File incoming = null;

	private File merged = null;

	private File failed = null;

	private int runInterval = DEFAULT_RUN_INTERVAL_MS;
	
	private ArcIndexer indexer = new ArcIndexer();
	
	/**
	 * Thread object of update thread -- also is flag indicating if the thread
	 * has already been started -- static, and access to it is synchronized.
	 */
	private static Thread updateThread = null;
	/**
	 * Default constructor
	 */
	public BDBIndexUpdater() {
		
	}
	/**
	 * @param index
	 * @param incoming
	 */
	public BDBIndexUpdater(BDBIndex index, File incoming) {
		this.index = index;
		this.incoming = incoming;
	}

	/**
	 * start the background index merging thread
	 * @throws ConfigurationException
	 */
	public void init() throws ConfigurationException {
		if(index == null) {
			throw new ConfigurationException("No index target on bdb updater");
		}
		if(incoming == null) {
			throw new ConfigurationException("No incoming on bdb updater");			
		}
		startUpdateThread();
	}
	
	/** Ensure the argument directory exists
	 * @param dir
	 * @throws IOException
	 */
	private void ensureDir(File dir) throws IOException {
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new IOException("FAILED to create " + dir.getAbsolutePath());
		}
	}
	
	/**
	 * start a background thread that merges new CDX files in incoming into
	 * the BDBIndex. 
	 * 
	 * @throws ConfigurationException
	 */
	public void startup() throws ConfigurationException {
		try {
			ensureDir(incoming);
			if(merged != null) ensureDir(merged);
			if(failed != null) ensureDir(failed);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
		
		if (updateThread == null) {
			startUpdateThread();
		}
	}
	
	/**
	 * start the BDBIndexUpdaterThread thread, which will scan for new cdx files
	 * in the incoming directory, and add them to the BDBIndex.
	 */
	private synchronized void startUpdateThread() {
		if (updateThread != null) {
			return;
		}
		updateThread = new BDBIndexUpdaterThread(this,runInterval);
		updateThread.start();
	}


	private boolean mergeFile(File cdxFile) {
		boolean added = false;
		try {
			Iterator it = indexer.getCDXFileBDBRecordIterator(cdxFile);
			index.insertRecords(it);
			added = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return added;
	}
	
	private File getTargetFile(File f, File targetDir) {
		File target = new File(targetDir, f.getName());
		int x = 0;
		while(target.exists()) {
			if(x++ > 255) {
				throw new RuntimeException("too many "
						+ "duplicates of file " + f.getAbsolutePath() +
						" in " + targetDir.getAbsolutePath());				
			}
			target = new File(targetDir,f.getName() + "." + x);
		}
		return target;
	}

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

	private void handleMerged(File f) {
		if (merged == null) {
			if (!f.delete()) {
				// big problems... lets exit
				throw new RuntimeException("Unable to delete "
						+ f.getAbsolutePath());
			}
			LOGGER.info("Removed merged file " + f.getAbsolutePath());
		} else {
			// move to merged:
			File target = getTargetFile(f,merged);
			if (!f.renameTo(target)) {
				throw new RuntimeException("FAILED rename" + "("
						+ f.getAbsolutePath() + ") to " + "("
						+ target.getAbsolutePath() + ")");
			}
			LOGGER.info("Renamed merged file " + f.getAbsolutePath() + " to " +
					target.getAbsolutePath());
		}
	}
	
	private void handleFailed(File f) {
		if (failed == null) {
			// nothing much to do.. just complain and leave it.
			LOGGER.info("FAILED INDEX: " + f.getAbsolutePath());
		} else {
			// move to failed:
			File target = getTargetFile(f,failed);
			if (!f.renameTo(target)) {
				throw new RuntimeException("FAILED rename" + "("
						+ f.getAbsolutePath() + ") to " + "("
						+ target.getAbsolutePath() + ")");
			}
			LOGGER.info("Renamed failed merge file " + f.getAbsolutePath() +
					" to " + target.getAbsolutePath());
		}
	}

	protected int mergeAll() {
		int numMerged = 0;
		File incomingFiles[] = incoming.listFiles();
		int i = 0;
		for (i = 0; i < incomingFiles.length; i++) {
			File f = incomingFiles[i];
			if (f.isFile()) {
				if (mergeFile(f)) {
					handleMerged(f);
					numMerged++;
				} else {
					handleFailed(f);
				}
			}
		}
		return numMerged;
	}

	/**
	 * @return the index
	 */
	public BDBIndex getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(BDBIndex index) {
		this.index = index;
	}

	/**
	 * @return the incoming
	 */
	public String getIncoming() {
		if(incoming == null) {
			return null;
		}
		return incoming.getAbsolutePath();
	}

	/**
	 * @param incoming the incoming to set
	 * @throws ConfigurationException 
	 */
	public void setIncoming(String incoming) throws ConfigurationException {
		this.incoming = ensureDir(incoming);
	}


	/**
	 * @return the merged
	 */
	public String getMerged() {
		if(merged == null) {
			return null;
		}
		return merged.getAbsolutePath();
	}

	/**
	 * @param merged The merged to set.
	 * @throws ConfigurationException 
	 */
	public void setMerged(String merged) throws ConfigurationException {
		this.merged = ensureDir(merged);
	}
	/**
	 * @param merged
	 * @throws IOException
	 */
	public void setMerged(File merged) throws IOException {
		ensureDir(merged);
		this.merged = merged;
	}

	/**
	 * @return the failed
	 */
	public String getFailed() {
		if(failed == null) {
			return null;
		}
		return failed.getAbsolutePath();
	}

	/**
	 * @param failed The failed to set.
	 * @throws ConfigurationException 
	 */
	public void setFailed(String failed) throws ConfigurationException {
		this.failed = ensureDir(failed);
	}
	/**
	 * @param failed
	 * @throws IOException
	 */
	public void setFailed(File failed) throws IOException {
		ensureDir(failed);
		this.failed = failed;
	}

	/**
	 * @return the runInterval
	 */
	public int getRunInterval() {
		return runInterval;
	}

	/**
	 * @param runInterval The runInterval to set.
	 */
	public void setRunInterval(int runInterval) {
		this.runInterval = runInterval;
	}
	/**
	 * Thread that repeatedly calls mergeAll on the BDBIndexUpdater.
	 * 
	 * @author Brad Tofel
	 * @version $Date$, $Revision$
	 */
	private class BDBIndexUpdaterThread extends Thread {
		/**
		 * object which merges CDX files with the BDBResourceIndex
		 */
		private BDBIndexUpdater updater = null;

		private int runInterval;

		/**
		 * @param updater
		 * @param runInterval
		 */
		public BDBIndexUpdaterThread(BDBIndexUpdater updater, int runInterval) {
			super("BDBIndexUpdaterThread");
			super.setDaemon(true);
			this.updater = updater;
			this.runInterval = runInterval;
			LOGGER.info("BDBIndexUpdaterThread is alive.");
		}

		public void run() {
			int sleepInterval = runInterval;
			while (true) {
				try {
					int numMerged = updater.mergeAll();
					if (numMerged == 0) {
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
}
