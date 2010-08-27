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
package org.archive.wayback.resourceindex.updater;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.archive.wayback.Shutdownable;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.cdx.CDXLineToSearchResultAdapter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.DirMaker;
import org.archive.wayback.util.flatfile.FlatFile;

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
public class LocalResourceIndexUpdater implements Shutdownable {
	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER =
	        Logger.getLogger(LocalResourceIndexUpdater.class.getName());

	private final static int DEFAULT_RUN_INTERVAL_MS = 10000;

	private LocalResourceIndex index = null;

	private File incoming = null;

	private File merged = null;

	private File failed = null;

	private int runInterval = DEFAULT_RUN_INTERVAL_MS;
	
	/**
	 * Thread object of update thread -- also is flag indicating if the thread
	 * has already been started. Access to it is synchronized.
	 */
	private Thread thread = null;

	/**
	 * start the background index merging thread
	 * @throws ConfigurationException
	 */
	public void init() throws ConfigurationException {
		if(index == null) {
			throw new ConfigurationException("No index target");
		}
		if(!index.isUpdatable()) {
			throw new ConfigurationException("ResourceIndex is not updatable");
		}
		if(incoming == null) {
			throw new ConfigurationException("No incoming");			
		}
		if(runInterval > 0) {
			thread = new UpdateThread(this,runInterval);
			thread.start();
		}
	}
	
	public void shutdown() {
		if(thread != null) {
			thread.interrupt();
			try {
				thread.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean mergeFile(File cdxFile) {
		boolean added = false;
		try {
			FlatFile ffile = new FlatFile(cdxFile.getAbsolutePath());
			AdaptedIterator<String,CaptureSearchResult> searchResultItr = 
				new AdaptedIterator<String,CaptureSearchResult>(
						ffile.getSequentialIterator(),
					new CDXLineToSearchResultAdapter());
			index.addSearchResults(searchResultItr);
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
	public LocalResourceIndex getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(LocalResourceIndex index) {
		this.index = index;
	}

	/**
	 * @return the incoming directory path, or null if not set 
	 */
	public String getIncoming() {
		return DirMaker.getAbsolutePath(incoming);
	}

	/**
	 * @param incoming the incoming to set
	 * @throws IOException 
	 */
	public void setIncoming(String incoming) throws IOException {
		this.incoming = DirMaker.ensureDir(incoming);
	}

	/**
	 * @return the merged directory path, or null if not set
	 */
	public String getMerged() {
		return DirMaker.getAbsolutePath(merged);
	}

	/**
	 * @param merged
	 * @throws IOException
	 */
	public void setMerged(String merged) throws IOException {
		this.merged = DirMaker.ensureDir(merged);
	}

	/**
	 * @return the failed directory path, or null if not set
	 */
	public String getFailed() {
		return DirMaker.getAbsolutePath(failed);
	}

	/**
	 * @param failed The failed to set.
	 * @throws IOException 
	 */
	public void setFailed(String failed) throws IOException {
		this.failed = DirMaker.ensureDir(failed);
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
	private class UpdateThread extends Thread {
		/**
		 * object which merges CDX files with the BDBResourceIndex
		 */
		private LocalResourceIndexUpdater updater = null;

		private int runInterval;

		/**
		 * @param updater
		 * @param runInterval
		 */
		public UpdateThread(LocalResourceIndexUpdater updater,
				int runInterval) {
			
			super("LocalResourceIndexUpdater.UpdateThread");
			super.setDaemon(true);
			this.updater = updater;
			this.runInterval = runInterval;
			LOGGER.info("LocalResourceIndexUpdater.UpdateThread is alive.");
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
					LOGGER.info("Shutting Down.");
					return;
				}
			}
		}
	}
}
