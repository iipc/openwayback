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
package org.archive.wayback.accesscontrol.staticmap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.surt.SURTTokenizer;
import org.archive.wayback.util.flatfile.FlatFile;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StaticMapExclusionFilterFactory implements ExclusionFilterFactory {
	private static final Logger LOGGER =
        Logger.getLogger(StaticMapExclusionFilterFactory.class.getName());

	private int checkInterval = 0;
	private Map<String,Object> currentMap = null;
	private File file = null;
	
	long lastUpdated = 0;
		
	private UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

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
			currentMap = loadFile(file.getAbsolutePath());
			lastUpdated = currentMod;
			LOGGER.info("Reload " + file.getAbsolutePath() + " OK");
		} catch(IOException e) {
			lastUpdated = -1;
			currentMap = null;
			e.printStackTrace();
			LOGGER.severe("Reload " + file.getAbsolutePath() + " FAILED:" + 
					e.getLocalizedMessage());
		}
	}
	protected Map<String,Object> loadFile(String path) throws IOException {
		Map<String, Object> newMap = new HashMap<String, Object>();
		FlatFile ff = new FlatFile(path);
		CloseableIterator<String> itr = ff.getSequentialIterator();
		while(itr.hasNext()) {
			String line = (String) itr.next();
			line = line.trim();
			if(line.length() == 0) {
				continue;
			}
			line = canonicalizer.urlStringToKey(line);
			String surt;
			
			if (canonicalizer.isSurtForm()) {
				surt = line;
			} else {
				surt = line.startsWith("(") ? line : 
				SURTTokenizer.prefixKey(line);
			}

			LOGGER.fine("EXCLUSION-MAP: adding " + surt);
			newMap.put(surt, null);
		}
		itr.close();
		return newMap;
	}
	
	/**
	 * @return ObjectFilter which blocks CaptureSearchResults in the 
	 * 						exclusion file. 
	 */
	public ExclusionFilter get() {
		if(currentMap == null) {
			return null;
		}
		return new StaticMapExclusionFilter(currentMap, canonicalizer); 
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
		private StaticMapExclusionFilterFactory service = null;

		private int runInterval;

		/**
		 * @param service ExclusionFactory which will be reloaded
		 * @param runInterval int number of seconds between reloads
		 */
		public CacheUpdaterThread(StaticMapExclusionFilterFactory service, int runInterval) {
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
