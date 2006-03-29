/* IndexPipeline
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.cdx.indexer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import org.archive.wayback.PropertyConfigurable;
import org.archive.wayback.cdx.BDBResourceIndex;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.exception.ConfigurationException;

import com.sleepycat.je.DatabaseException;

/**
 * Implements indexing of new ARC files, and merging with a BDBResourceIndex.
 * Assumes LocalBDBResourceIndex and LocalARCResourceStore for now.
 * Maintains state using directories and files for now.
 * 
 * There are 3 primary components, each could be a thread, but the steps are
 * run in serial for the moment:
 * 1) watch for new ARC files, and queue them for indexing
 * 2) index queued ARC files into CDX format, queue the CDX files for merging.
 * 3) merge queued CDX files with the ResourceIndex.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class IndexPipeline implements PropertyConfigurable{
	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER =
	        Logger.getLogger(IndexPipeline.class.getName());

	/**
	 * minimum number of milliseconds to sleep between scanning for new ARC
	 * files. If no new files have appeared, and the pipeline is idling, the
	 * pipeline thread will sleep for longer each iteration that there is no
	 * new work to do.  
	 */
	private final static int SLEEP_MILLISECONDS = 10000;
	
	/**
	 * maximum number of ARC files to index and merge each iteration.
	 */
	private final static int MAX_TO_MERGE = 10;

	/**
	 * Name of configuration for the name of this Wayback installation
	 */
	private final static String INSTALLATION_NAME = "installationname";
	
	/**
	 * Name of configuration for flag to activate pipeline thread
	 */
	private final static String RUN_PIPELINE = "indexpipeline.runpipeline";

	/**
	 * Name of configuration for directory containing BDBResourceIndex
	 */
	private final static String INDEX_PATH = "resourceindex.indexpath";

	/**
	 * Name of configuration for name of BDBJE database
	 */
	private final static String DB_NAME = "resourceindex.dbname";

	/**
	 * Name of configuration for directory containing ARC files
	 */
	private final static String ARC_PATH = "arcpath";

	/**
	 * Name of configuration for directory under which pipeline state is stored
	 */
	private final static String WORK_PATH = "indexpipeline.workpath";

	/**
	 * Name of Queued state directory, under WORK_PATH
	 */
	private final static String QUEUED_DIR = "queued";

	/**
	 * Name of To Be Indexed state directory, under WORK_PATH
	 */
	private final static String TO_BE_INDEXED_DIR = "toBeIndexed";
	
	/**
	 * Name of Indexing state directory, under WORK_PATH
	 */
	private final static String INDEXING_DIR = "indexing";
	
	/**
	 * Name of To Be Merged state directory, under WORK_PATH
	 */
	private final static String TO_BE_MERGED_DIR = "toBeMerged";

	/**
	 * Name of Merged state directory, under WORK_PATH
	 */
	private final static String MERGED_DIR = "merged";
	
	
	/**
	 * Name of this Wayback installation
	 */
	private String installationName = "Unknown";

	/**
	 * File object of arc directory
	 */
	private File arcDir = null;
	
	/**
	 * File object of working directory
	 */
	private File workDir = null;

	/**
	 * File Object of queued directory
	 */
	private File queuedDir = null;

	/**
	 * File Object of To Be Indexed directory
	 */
	private File toBeIndexedDir = null;

	/**
	 * File Object of Indexing directory
	 */
	private File indexingDir = null;

	/**
	 * File Object of To Be Merged directory
	 */
	private File toBeMergedDir = null;

	/**
	 * File Object of Merged directory
	 */
	private File mergedDir = null;

	/**
	 * object holding ResourceIndex
	 */
	private BDBResourceIndex db = null;

	/**
	 * Thread object of update thread -- also is flag indicating if the thread
	 * has already been started -- static, and access to it is synchronized.
	 */
	private static Thread indexUpdateThread = null;
	

	/**
	 * Constructor
	 */
	public IndexPipeline() {
		super();
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
	 * Initialize this object, creating directories if needed, and starting
	 * thread if configured.
	 * 
	 * @param p configuration 
	 * @throws ConfigurationException
	 */
	public void init(Properties p) throws ConfigurationException {
		
		// what is the (optional) name of this installation?
		String installationName = (String) p.get(INSTALLATION_NAME);
		if (installationName != null && installationName.length() > 0) {
			this.installationName = installationName;
		}

		// where do we find ARC files?
		String arcPath = (String) p.get(ARC_PATH);
		if (arcPath != null) {
			arcDir = new File(arcPath);
		}

		// where is the BDB? (and what is it named?)
		String dbPath = (String) p.get(INDEX_PATH);
		if (dbPath == null || (dbPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + INDEX_PATH);
		}

		String dbName = (String) p.get(DB_NAME);
		if (dbName == null || (dbName.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + DB_NAME);
		}
		
		// where do we keep working files?
		String workPath = (String) p.get(WORK_PATH);
		if (workPath == null || (workPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + WORK_PATH);
		}

		workDir = new File(workPath);
		queuedDir = new File(workDir,QUEUED_DIR);
		toBeIndexedDir = new File(workDir,TO_BE_INDEXED_DIR);
		indexingDir = new File(workDir,INDEXING_DIR);
		toBeMergedDir = new File(workDir,TO_BE_MERGED_DIR);
		mergedDir = new File(workDir,MERGED_DIR);

		try {
			ensureDir(workDir);
			ensureDir(queuedDir);
			ensureDir(toBeIndexedDir);
			ensureDir(indexingDir);
			ensureDir(toBeMergedDir);
			ensureDir(mergedDir);
			File dbFile = new File(dbPath);
			ensureDir(dbFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}

		String runPipeline = (String) p.get(RUN_PIPELINE);
		try {
			db = new BDBResourceIndex(dbPath, dbName);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}

		if ((runPipeline != null) && (runPipeline.equals("1"))) {

			LOGGER.info("LocalDBDResourceIndex starting pipeline thread...");
			if (indexUpdateThread == null) {
				startIndexPipelineThread(db);
			}
		}
	}
	
	/**
	 */
	public void shutdown() {
		if(db != null) {
			try {
				db.shutdownDB();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				// how to handle??
				e.printStackTrace();
			}
		}
	}
	
	/** start the IndexPipeline thread, which will scan for new arcs, index
	 * new arcs that appear, and merge indexed arcs (in CDX format) into the
	 * BDBResourceIndex
	 * 
	 * @param bdb
	 */
	private synchronized void startIndexPipelineThread(
			final BDBResourceIndex bdb) {
		if (indexUpdateThread != null) {
			return;
		}
		indexUpdateThread = new IndexPipelineThread(bdb, this);
		indexUpdateThread.start();
	}

	/**
	 * @return a HashMap with String keys of all ARC files which have already
	 * been queued for indexing.
	 */
	private HashMap getQueuedFiles() {
		HashMap hash = new HashMap();
		String entries[] = queuedDir.list();
		for (int i = 0; i < entries.length; i++) {
			hash.put(entries[i], "i");
		}
		return hash;
	}

	/**
	 * @param dir
	 * @return anIteratory of File objects of all regular files in dir
	 */
	private Iterator getDirFilesIterator(File dir) {
		String files[] = dir.list();
		ArrayList list = new ArrayList();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = new File(dir, files[i]);
				if (file.isFile()) {
					list.add(files[i]);
				}
			}
		}
		return list.iterator();
	}

	// 
	/** return any new ARC files in the ARCs directory. this should be a method 
	 * call into ResourceStore...
	 * @return an Iterator of Strings of filenames of new ARCs.
	 */
	private Iterator getNewArcs() {
		HashMap queued = getQueuedFiles();
		ArrayList newArcs = new ArrayList();

		String arcs[] = arcDir.list();
		if (arcs != null) {
			for (int i = 0; i < arcs.length; i++) {
				File arc = new File(arcDir,arcs[i]);
				if(arc.isFile() && arcs[i].endsWith(".arc.gz")) {
					
					if (!queued.containsKey(arcs[i])) {
						newArcs.add(arcs[i]);
					}
				}
			}
		}
		return newArcs.iterator();
	}

	/** update pipeline state to indicate that an ARC needs to be indexed, and
	 * has been queued for indexing already. 
	 * @param newArc
	 * @throws IOException
	 */
	private void queueArcForIndex(final String newArc) throws IOException {
		File newQueuedFile = new File(queuedDir,newArc);
		File newToBeIndexedFile = new File(toBeIndexedDir,newArc);
		newToBeIndexedFile.createNewFile();
		newQueuedFile.createNewFile();
	}

	/**
	 * Find any new ARC files and queue them for indexing. 
	 * @throws IOException
	 */
	public void queueNewArcsForIndex() throws IOException {
		if(arcDir == null) {
			return;
		}
		Iterator newArcs = getNewArcs();
		while(newArcs.hasNext()) {
			String newArc = (String) newArcs.next();
			queueArcForIndex(newArc);
		}
	}
	
	/**
	 * Index up to 'max' ARC files queued for indexing, queueing the resulting 
	 * CDX files for merging with the BDBResourceIndex.
	 * 
	 * @param indexer
	 * @param max maximum number to index in this method call, 0 for unlimited
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void indexArcs(ArcIndexer indexer, int max) 
	throws MalformedURLException, IOException {
		if(arcDir == null) {
			return;
		}
		Iterator toBeIndexed = getDirFilesIterator(toBeIndexedDir);
		int numIndexed = 0;
		while(toBeIndexed.hasNext()) {
			String base = (String) toBeIndexed.next();

			File arcFile = new File(arcDir,base);
			File toBeIndexedFlagFile = new File(toBeIndexedDir,base);
			File indexFile = new File(indexingDir,base);
			File toBeMergedFile = new File(toBeMergedDir,base);

			LOGGER.info("Indexing ARC " + arcFile.getAbsolutePath());
			SearchResults res = indexer.indexArc(arcFile);
			LOGGER.info("Serializing ARC data in " + 
					indexFile.getAbsolutePath());
			indexer.serializeResults(res, indexFile);
			if (!indexFile.renameTo(toBeMergedFile)) {
				throw new IOException("Unable to move "
						+ indexFile.getAbsolutePath() + " to "
						+ toBeMergedFile.getAbsolutePath());
			}
			if (!toBeIndexedFlagFile.delete()) {
				throw new IOException("Unable to delete "
						+ toBeIndexedFlagFile.getAbsolutePath());
			}
			numIndexed++;
			if(max > 0 && (numIndexed >= max)) {
				break;
			}
		}
	}
	/**
	 * Index all ARC files queued for indexing, queueing the resulting CDX files
	 * for merging with the BDBResourceIndex.
	 * 
	 * @param indexer
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void indexArcs(ArcIndexer indexer)
		throws MalformedURLException, IOException {
		indexArcs(indexer,0);
	}
	
	/**
	 * Add any new CDX files in toBeMergedDir to the BDB, deleting the CDX
	 * files as they are merged
	 * For now, moving merged to "merged" for debugging..
	 * @param dbWriter
	 * @return int number of ARC files indexed
	 */
	public int mergeIndex(BDBResourceIndexWriter dbWriter) {
		int numMerged = 0;
		Iterator toBeMerged = getDirFilesIterator(toBeMergedDir);
		while(toBeMerged.hasNext()) {
			
			String base = (String) toBeMerged.next();
			File indexFile = new File(toBeMergedDir,base);
			File mergedFile = new File(mergedDir,base);
			try {
				LOGGER.info("Importing ARC data from " +
						indexFile.getAbsolutePath());
				dbWriter.importFile(indexFile);
				
				// move to "merged" for debugging
				if (!indexFile.renameTo(mergedFile)) {
					throw new IOException("Unable to move "
							+ indexFile.getAbsolutePath() + " to "
							+ mergedFile.getAbsolutePath());
				}
//				if (!indexFile.delete()) {
//					throw new IOException("Unable to unlink "
//							+ indexFile.getAbsolutePath());
//				}
				numMerged++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (numMerged > 0) {
			LOGGER.info("Merged " + numMerged + " files.");
		}
		return numMerged;
	}

	/**
	 * Gather a snapshot of the pipeline in a PipelineStatus object.
	 * @return PipelineStatus
	 */
	public PipelineStatus getStatus() {
		PipelineStatus status = new PipelineStatus();
		String index[] = toBeIndexedDir.list();
		String merge[] = toBeMergedDir.list();
//		String queued[] = queuedDir.list();
		String merged[] = mergedDir.list();
		String numQueuedForIndex = (index == null) ? "0" : "" + index.length;
		String numQueuedForMerge = (merge == null) ? "0" : "" + merge.length;

		
		// for now, we're keeping a copy of the indexed files, so we will
		// use the length of the mergedDir array.. Eventually, we will be
		// unlinking the files as they are indexed, in which case, we will
		// have to do this math:
//		String numIndexed = (queued == null) ? "0" : "" + (queued.length - 
//				(index.length + merge.length)); 
		String numIndexed = (merged == null) ? "0" : "" + merged.length;

		status.setNumQueuedForIndex(numQueuedForIndex);
		status.setNumQueuedForMerge(numQueuedForMerge);
		status.setNumIndexed(numIndexed);
		
		status.setArcPath((arcDir == null ? "NONE" : arcDir.getAbsolutePath()));
		status.setDatabasePath(db.getPath());
		status.setDatabaseName(db.getDbName());
//		status.setDbFileSize(db.getJE_LOG_FILEMAX());
		status.setInstallationName(this.installationName);
		status.setPipelineActive(indexUpdateThread != null);
		status.setPipelineWorkPath(this.workDir.getAbsolutePath());
		return status;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	///////////////////////////////////////////////////
	// IndexPipelineThread Class
	///////////////////////////////////////////////////
	
	/**
	 * Thread that repeatedly runs processing of an IndexPipeline and merges new
	 * data into a BDBResourceIndex
	 * 
	 * @author Brad Tofel
	 * @version $Date$, $Revision$
	 */
	private class IndexPipelineThread extends Thread {
		/**
		 * object which merges CDX files with the BDBResourceIndex
		 */
		private BDBResourceIndexWriter merger = null;
		/**
		 * object which indexes ARC files, and writes CDX
		 */
		private ArcIndexer indexer = new ArcIndexer();
		/**
		 * IndexPipeline object
		 */
		IndexPipeline pipeline = null;

		/**
		 * Constructor
		 * 
		 * @param bdb
		 *            initialized BDBResourceIndex
		 * @param pipeline
		 *            initialized IndexPipeline
		 */
		public IndexPipelineThread(final BDBResourceIndex bdb,
				IndexPipeline pipeline) {
			super("IndexPipelineThread");
			super.setDaemon(true);
			merger = new BDBResourceIndexWriter();
			merger.init(bdb);
			this.pipeline = pipeline;
			LOGGER.info("Pipeline Thread is ALIVE!");
		}

		public void run() {

			int sleepInterval = SLEEP_MILLISECONDS;
			while (true) {
				try {
					pipeline.queueNewArcsForIndex();
					pipeline.indexArcs(indexer,MAX_TO_MERGE);
					int numMerged = pipeline.mergeIndex(merger);
					if(numMerged == 0) {
						sleep(sleepInterval);
						sleepInterval += SLEEP_MILLISECONDS;
					} else {
						sleepInterval = SLEEP_MILLISECONDS;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return Returns the indexingDir.
	 */
	public File getIndexingDir() {
		return indexingDir;
	}

	/**
	 * @return Returns the toBeMergedDir.
	 */
	public File getToBeMergedDir() {
		return toBeMergedDir;
	}
}
