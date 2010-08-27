/* IndexQueueUpdater
 *
 * $Id$
 *
 * Created on 2:02:54 PM Jun 23, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import org.archive.wayback.Shutdownable;
import org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDB;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.DirMaker;

/**
 * This class polls a ResourceFileLocationDB repeatedly, to notice new files
 * arriving in the DB. Whenever new files are noticed, they are added to the
 * Index Queue.
 * 
 * It uses a local file to store the last known "mark" of the location DB.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class IndexQueueUpdater implements Shutdownable {

	private static final Logger LOGGER =
        Logger.getLogger(IndexQueueUpdater.class.getName());

	private ResourceFileLocationDB db = null;
	private IndexQueue queue = null;
	private UpdateThread thread = null;
	private MarkMemoryFile lastMark = null;
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

	public int updateQueue() throws IOException {
		int added = 0;
		long lastMarkPoint = lastMark.getLastMark();
		long currentMarkPoint = db.getCurrentMark();
		if(currentMarkPoint > lastMarkPoint) {
			// TODO: touchy touchy... need transactions here to not have 
			// state sync problems if something goes badly in this block..
			// for example, it would be possible to constantly enqueue the
			// same files forever..
			CloseableIterator<String> newNames = 
				db.getNamesBetweenMarks(lastMarkPoint, currentMarkPoint);
			while(newNames.hasNext()) {
				String newName = newNames.next();
				LOGGER.info("Queued " + newName + " for indexing.");
				queue.enqueue(newName);
				added++;
			}
			newNames.close();
			lastMark.setLastMark(currentMarkPoint);
		}
		return added;
	}

	private class MarkMemoryFile {
		private File file = null;
		public MarkMemoryFile(File file) {
			this.file = file;
		}

		public long getLastMark() throws IOException {
			long mark = 0;
			if(file.isFile() && file.length() > 0) {
				BufferedReader ir = new BufferedReader(new FileReader(file));
				String line = ir.readLine();
				if(line != null) {
					mark = Long.parseLong(line);
				}
			}
			return mark;
		}
		
		public void setLastMark(long mark) throws IOException {
			PrintWriter pw = new PrintWriter(file);
			pw.println(mark);
			pw.close();
		}
		public String getAbsolutePath() {
			return file.getAbsolutePath();
		}
	}
	
	private class UpdateThread extends Thread {
		private long runInterval = 120000;
		private IndexQueueUpdater updater = null;

		public UpdateThread(IndexQueueUpdater updater,
				long runInterval) {

			this.updater = updater;
			this.runInterval = runInterval;
		}

		public void run() {
			LOGGER.info("alive");
			long sleepInterval = runInterval;
			while (true) {
				try {
					int updated = updater.updateQueue();
					
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
	 * @return the queue
	 */
	public IndexQueue getQueue() {
		return queue;
	}

	/**
	 * @param queue the queue to set
	 */
	public void setQueue(IndexQueue queue) {
		this.queue = queue;
	}

	/**
	 * @return the stateFile
	 */
	public String getLastMark() {
		if(lastMark != null) {
			return lastMark.getAbsolutePath();
		}
		return null;
	}

	/**
	 * @param stateFile the stateFile to set
	 * @throws IOException 
	 */
	public void setLastMark(String path) throws IOException {
		File tmp = new File(path);
		DirMaker.ensureDir(tmp.getParentFile().getAbsolutePath());
		lastMark = new MarkMemoryFile(tmp);
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
