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
package org.archive.wayback.resourcestore.resourcefile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.archive.wayback.Shutdownable;
import org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDBUpdater;
import org.archive.wayback.util.DirMaker;

/**
 * Class which repeatedly builds a ResourceFileList for a set of 
 * ResourceFileSource objects, serializing them into files, and dropping them
 * into the incoming directory of a ResourceFileLocationDBUpdater.
 * 
 * In the current implementation, this uses only a single thread to scan the
 * ResourceFileSource objects, but with larger installations (1000's of
 * ResourceFileSources), multiple threads may later be required.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFileSourceUpdater implements Shutdownable {
	private static final Logger LOGGER =
        Logger.getLogger(ResourceFileSourceUpdater.class.getName());
	private List<ResourceFileSource> sources = null;

	private File target = null;
	
	
	private UpdateThread thread = null;
	private long interval = 120000;
	
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

	private void synchronizeSource(ResourceFileSource source) {
		String name = source.getName();
		try {
			LOGGER.info("Synchronizing " + name);
			ResourceFileList list = source.getResourceFileList();
			String tmp = name + ResourceFileLocationDBUpdater.TMP_SUFFIX; 
			File tmpListTarget = new File(target,tmp);
			File listTarget = new File(target,name);
			list.store(tmpListTarget);
			tmpListTarget.renameTo(listTarget);
			LOGGER.info("Synchronized " + name);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.warning("FAILED Synchronize " + name + e.getMessage());
		}
	}
	
	public void synchronizeSources() {
		for(ResourceFileSource source : sources) {
			synchronizeSource(source);
		}
	}

	private class UpdateThread extends Thread {
		private long runInterval = 120000;
		private ResourceFileSourceUpdater updater = null;

		public UpdateThread(ResourceFileSourceUpdater updater,
				long runInterval) {

			this.updater = updater;
			this.runInterval = runInterval;
		}

		public void run() {
			LOGGER.info("alive");
			while (true) {
				try {
					long startSync = System.currentTimeMillis();
					updater.synchronizeSources();
					long endSync = System.currentTimeMillis();
					long syncDuration = endSync - startSync;
					long sleepInterval = runInterval - syncDuration;
					if(sleepInterval > 0) {
						sleep(sleepInterval);
					} else {
						LOGGER.warning("Last Synchronize took " + syncDuration +
								" where interval is " + interval + 
								". Not sleeping.");
					}
				} catch (InterruptedException e) {
					LOGGER.info("Shutting Down.");
					return;
				}
			}
		}
	}
	
	public List<ResourceFileSource> getSources() {
		return sources;
	}

	public void setSources(List<ResourceFileSource> sources) {
		this.sources = sources;
	}

	public String getTarget() {
		return DirMaker.getAbsolutePath(target);
	}

	public void setTarget(String target) throws IOException {
		this.target = DirMaker.ensureDir(target);
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
