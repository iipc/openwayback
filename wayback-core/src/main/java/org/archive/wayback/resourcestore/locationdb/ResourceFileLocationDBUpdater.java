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
package org.archive.wayback.resourcestore.locationdb;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.wayback.Shutdownable;
import org.archive.wayback.resourcestore.resourcefile.ResourceFileList;
import org.archive.wayback.resourcestore.resourcefile.ResourceFileLocation;
import org.archive.wayback.util.DirMaker;

/**
 * Class which performs updates on a ResourceFileLocationDB, based on files
 * appearing in a incoming directory. When files are noticed in the "incoming"
 * directory, they are assumed to be in the format serialized by
 *   org.archive.wayback.resourcestore.resourcefile.ResourceFileList
 * 
 * These files are synchronized with the ResourceFileLocationDB, and deleted.
 * 
 * Each file has a logical name, which is assumed to uniquely identify a
 * ResourceFileSource. As an optimization, the last state of each 
 * ResouceFileSource is kept in a file under the "state" directory.
 * 
 * This allows this class to compute a difference of the last state with the
 * new files in incoming, and only deltas: new files, removed files, 
 * and possibly moved files, need to applied to the ResourceFileLocationDB.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFileLocationDBUpdater implements Shutdownable {
	private static final Logger LOGGER =
        Logger.getLogger(ResourceFileLocationDBUpdater.class.getName());

	private ResourceFileLocationDB db = null;
	private File stateDir = null;
	private File incomingDir = null;
	private UpdateThread thread = null;
	private long interval = 120000;
	
	public final static String TMP_SUFFIX = ".TMP";

	public void init() {
		if(interval > 0) {
			thread = new UpdateThread(this,interval);
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
	
	public int synchronizeIncoming() throws IOException {
		File[] updates = incomingDir.listFiles();
		int updated = 0;
		for(File update : updates) {
			if(update.getName().endsWith(TMP_SUFFIX)) {
				continue;
			}
			if(synchronize(update)) {
				updated++;
			}
		}
		return updated;
	}
	
	public boolean synchronize(File update) throws IOException {
		String name = update.getName();
		File current = new File(stateDir,name);
		if(!current.isFile()) {
			current.createNewFile();
		}
		ResourceFileList updateFL = ResourceFileList.load(update);
		ResourceFileList currentFL = ResourceFileList.load(current);
		
		boolean updated = false;
		
		ResourceFileList removedFiles = currentFL.subtract(updateFL);
		ResourceFileList addedFiles = updateFL.subtract(currentFL);
		
		Iterator<ResourceFileLocation> addedItr = addedFiles.iterator();
		Iterator<ResourceFileLocation> removedItr = removedFiles.iterator();
		while(addedItr.hasNext()) {
			updated = true;
			ResourceFileLocation location = addedItr.next();
			LOGGER.info("Added " + location.getName() + " " + location.getUrl());
			db.addNameUrl(location.getName(), location.getUrl());
		}
		while(removedItr.hasNext()) {
			updated = true;
			ResourceFileLocation location = removedItr.next();
			LOGGER.info("Removed " + location.getName() + " " + location.getUrl());
			db.removeNameUrl(location.getName(), location.getUrl());
		}
		if(updated) {
			// lastly replace the state file with the new version:
			if(!current.delete()) {
				throw new IOException("Unable to delete " + 
						current.getAbsolutePath());
			}
			if(!update.renameTo(current)) {
				throw new IOException("Unable to rename " + 
						update.getAbsolutePath() + " to " + 
						current.getAbsolutePath());
			}
		} else {
			if(!update.delete()) {
				throw new IOException("Unable to delete " + 
						update.getAbsolutePath());
			}
		}
		return updated;
	}
	
	private class UpdateThread extends Thread {
		private long runInterval = 120000;
		private ResourceFileLocationDBUpdater updater = null;
		
		public UpdateThread(ResourceFileLocationDBUpdater updater, long runInterval) {
			this.updater = updater;
			this.runInterval = runInterval;
		}
		public void run() {
			LOGGER.info("ResourceFileLocationDBUpdater.UpdateThread is alive.");
			long sleepInterval = runInterval;
			while (true) {
				try {
					int updated = updater.synchronizeIncoming();
					
					if(updated > 0) {
						sleepInterval = runInterval;
					} else {
						sleepInterval += runInterval;
					}
					sleep(sleepInterval);
				} catch (InterruptedException e) {
					LOGGER.info("Shutting Down.");
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return the db
	 */
	public ResourceFileLocationDB getDb() {
		return db;
	}
	/**
	 * @param db the db to set
	 */
	public void setDb(ResourceFileLocationDB db) {
		this.db = db;
	}
	/**
	 * @return the stateDir
	 */
	public String getStateDir() {
		return DirMaker.getAbsolutePath(stateDir);
	}
	/**
	 * @param stateDir the stateDir to set
	 * @throws IOException 
	 */
	public void setStateDir(String stateDir) throws IOException {
		this.stateDir = DirMaker.ensureDir(stateDir);
	}
	/**
	 * @return the incomingDir
	 */
	public String getIncomingDir() {
		return DirMaker.getAbsolutePath(incomingDir);
	}
	/**
	 * @param incomingDir the incomingDir to set
	 * @throws IOException 
	 */
	public void setIncomingDir(String incomingDir) throws IOException {
		this.incomingDir = DirMaker.ensureDir(incomingDir);
	}
	/**
	 * @return the interval
	 */
	public long getInterval() {
		return interval;
	}
	/**
	 * @param interval the interval to set
	 */
	public void setInterval(long interval) {
		this.interval = interval;
	}
}
