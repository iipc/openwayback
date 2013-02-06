package org.archive.wayback.accesscontrol.staticmap;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.archive.util.SURT;
import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.flatfile.FlatFile;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

public class StaticListExclusionFilterFactory implements ExclusionFilterFactory {
	private static final Logger LOGGER =
        Logger.getLogger(StaticMapExclusionFilterFactory.class.getName());

	private int checkInterval = 0;
	private TreeSet<String> excludes = null;
	private File file = null;
	long lastUpdated = 0;
	UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();

	/**
	 * Thread object of update thread -- also is flag indicating if the thread
	 * has already been started -- static, and access to it is synchronized.
	 */
	private static Thread updateThread = null;
	
	/**
	 * load exclusion file and startup polling thread to check for updates
	 * @throws IOException if the exclusion file could not be read.
	 */
	public void init() throws IOException {
		reloadFile();
		if(checkInterval > 0) {
			startUpdateThread();
		}
	}

	protected void reloadFile() throws IOException {
		long currentMod = file.lastModified();
		if(currentMod == lastUpdated) {
			if(currentMod == 0) {
				LOGGER.severe("No exclude file at " + file.getAbsolutePath());
			}
			return;
		}
		LOGGER.info("Reloading exclusion file " + file.getAbsolutePath());
		try {
			excludes = loadFile(file.getAbsolutePath());
			lastUpdated = currentMod;
			LOGGER.info("Reload " + file.getAbsolutePath() + " OK");
		} catch(IOException e) {
			lastUpdated = -1;
			excludes = null;
			e.printStackTrace();
			LOGGER.severe("Reload " + file.getAbsolutePath() + " FAILED:" + 
					e.getLocalizedMessage());
		}
	}
	protected TreeSet<String> loadFile(String path) throws IOException {
		TreeSet<String> excludes = new TreeSet<String>();
		FlatFile ff = new FlatFile(path);
		CloseableIterator<String> itr = ff.getSequentialIterator();
		while(itr.hasNext()) {
			String line = (String) itr.next();
			line = line.trim();
			if(line.length() == 0) {
				continue;
			}
			line = canonicalizer.urlStringToKey(line);
			String surt = line.startsWith("(") ? line : SURT.fromPlain(line);
//				SURTTokenizer.prefixKey(line);
			LOGGER.fine("EXCLUSION-MAP: adding " + surt);
			excludes.add(surt);
		}
		itr.close();
		return excludes;
	}
	
	/**
	 * @return ObjectFilter which blocks CaptureSearchResults in the 
	 * 						exclusion file. 
	 */
	public ExclusionFilter get() {
		if(excludes == null) {
			return null;
		}
		return new StaticListExclusionFilter(excludes, canonicalizer); 
	}
	
	private synchronized void startUpdateThread() {
		if (updateThread != null) {
			return;
		}
		updateThread = new CacheUpdaterThread(this,checkInterval);
		updateThread.start();
	}
	private synchronized void stopUpdateThread() {
		if (updateThread == null) {
			return;
		}
		updateThread.interrupt();
	}
	
	private class CacheUpdaterThread extends Thread {
		/**
		 * object which merges CDX files with the BDBResourceIndex
		 */
		private StaticListExclusionFilterFactory service = null;

		private int runInterval;

		/**
		 * @param service ExclusionFactory which will be reloaded
		 * @param runInterval int number of seconds between reloads
		 */
		public CacheUpdaterThread(StaticListExclusionFilterFactory service, int runInterval) {
			super("CacheUpdaterThread");
			super.setDaemon(true);
			this.service = service;
			this.runInterval = runInterval;
			LOGGER.info("CacheUpdaterThread is alive.");
		}

		public void run() {
			int sleepInterval = runInterval;
			while (true) {
				try {
					try {
						service.reloadFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Thread.sleep(sleepInterval * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * @return the checkInterval in seconds
	 */
	public int getCheckInterval() {
		return checkInterval;
	}

	/**
	 * @param checkInterval the checkInterval in seconds to set
	 */
	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}

	/**
	 * @return the path
	 */
	public String getFile() {
		return file.getAbsolutePath();
	}

	/**
	 * @param path the file to set
	 */
	public void setFile(String path) {
		this.file = new File(path);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.accesscontrol.ExclusionFilterFactory#shutdown()
	 */
	public void shutdown() {
		stopUpdateThread();
	}

}
