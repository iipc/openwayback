/* CachedMapExclusionService
 *
 * $Id$
 *
 * Created on 6:49:42 PM Mar 5, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.staticmap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.surt.SURTTokenizer;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.flatfile.FlatFile;

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
	/**
	 * Thread object of update thread -- also is flag indicating if the thread
	 * has already been started -- static, and access to it is synchronized.
	 */
	private static Thread updateThread = null;
	
	/**
	 * load exclusion file and startup polling thread to check for updates
	 * @throws IOException 
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
			String surt = line.startsWith("(") ? line : 
				SURTTokenizer.prefixKey(line);
			LOGGER.fine("EXCLUSION-MAP: adding " + surt);
			newMap.put(surt, null);
		}
		itr.close();
		return newMap;
	}
	
	/**
	 * @param wbRequest 
	 * @return SearchResultFilter 
	 */
	public ObjectFilter<SearchResult> get() {
		if(currentMap == null) {
			return null;
		}
		return new StaticMapExclusionFilter(currentMap); 
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
		 * @param service 
		 * @param runInterval
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
