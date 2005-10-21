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

package org.archive.wayback.arcindexer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.localbdbresourceindex.BDBResourceIndex;

import com.sun.org.apache.xml.internal.utils.StringToStringTable;

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
public class IndexPipeline {
	private final static String RUN_PIPELINE = "indexpipeline.runpipeline";

	private final static String INDEX_PATH = "resourceindex.indexpath";

	private final static String DB_NAME = "resourceindex.dbname";

	private final static String ARC_PATH = "arcpath";

	private final static String WORK_PATH = "indexpipeline.workpath";

	private final static String QUEUED_DIR = "queued";

	private final static String TO_BE_INDEXED_DIR = "toBeIndexed";
	
	private final static String INDEXING_DIR = "indexing";
	
	private final static String TO_BE_MERGED_DIR = "toBeMerged";
	
	
	private File arcDir = null;
	
	private File workDir = null;

	private File queuedDir = null;

	private File toBeIndexedDir = null;

	private File indexingDir = null;

	private File toBeMergedDir = null;

	private BDBResourceIndex db = null;

	private static Thread indexUpdateThread = null;
	

	/**
	 * Constructor
	 */
	public IndexPipeline() {
		super();
	}

	private void ensureDir(File dir) throws IOException {
		if (!dir.isDirectory() && !dir.mkdir()) {
			throw new IOException("FAILED to create " + dir.getAbsolutePath());
		}
	}

	/**
	 * Initialize this object, creating directories if needed, and starting
	 * thread if configured.
	 * 
	 * @param p configuration 
	 * @throws IOException
	 */

	public void init(Properties p) throws IOException {
		
		// where do we find ARC files?
		String arcPath = (String) p.get(ARC_PATH);
		if (arcPath == null || (arcPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + ARC_PATH);
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

		String runPipeline = (String) p.get(RUN_PIPELINE);
		try {
			db = new BDBResourceIndex(dbPath, dbName);
		} catch (Exception e) {
			// TODO is this the right choice? was already obfuscated from BDBException...
			throw new IOException(e.getMessage());
		}
		arcDir = new File(arcPath);
		workDir = new File(workPath);
		queuedDir = new File(workDir,QUEUED_DIR);
		toBeIndexedDir = new File(workDir,TO_BE_INDEXED_DIR);
		indexingDir = new File(workDir,INDEXING_DIR);
		toBeMergedDir = new File(workDir,TO_BE_MERGED_DIR);

		ensureDir(workDir);
		ensureDir(queuedDir);
		ensureDir(toBeIndexedDir);
		ensureDir(indexingDir);
		ensureDir(toBeMergedDir);

		if ((runPipeline != null) && (runPipeline.equals("1"))) {

			System.out
					.println("LocalDBDResourceIndex starting pipeline thread...");
			if (indexUpdateThread == null) {
				startIndexPipelineThread(db);
			}
		}
	}
	
	private synchronized void startIndexPipelineThread(
			final BDBResourceIndex bdb) {
		if (indexUpdateThread != null) {
			return;
		}
		indexUpdateThread = new IndexPipelineThread(bdb, this);
		indexUpdateThread.start();
	}

	private StringToStringTable getQueuedFiles() {
		StringToStringTable hash = new StringToStringTable();
		String entries[] = queuedDir.list();
		for (int i = 0; i < entries.length; i++) {
			hash.put(entries[i], "i");
		}
		return hash;
	}

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

	// this should be a method call into ResourceStore...
	private Iterator getNewArcs() {
		StringToStringTable queued = getQueuedFiles();
		ArrayList newArcs = new ArrayList();

		String arcs[] = arcDir.list();
		if (arcs != null) {
			for (int i = 0; i < arcs.length; i++) {
				File arc = new File(arcDir,arcs[i]);
				if(arc.isFile() && arcs[i].endsWith(".arc.gz")) {
					if (!queued.contains(arcs[i])) {
						newArcs.add(arcs[i]);
					}
				}
			}
		}
		return newArcs.iterator();
	}

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
		Iterator newArcs = getNewArcs();
		while(newArcs.hasNext()) {
			String newArc = (String) newArcs.next();
			queueArcForIndex(newArc);
		}
	}
	
	/**
	 * Index any ARC files queued for indexing, queueing the resulting CDX files
	 * for merging with the BDBResourceIndex.
	 * 
	 * @param indexer
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void indexArcs(ArcIndexer indexer) throws MalformedURLException, IOException {
		Iterator toBeIndexed = getDirFilesIterator(toBeIndexedDir);
		while(toBeIndexed.hasNext()) {
			String base = (String) toBeIndexed.next();

			File arcFile = new File(arcDir,base);
			File toBeIndexedFlagFile = new File(toBeIndexedDir,base);
			File indexFile = new File(indexingDir,base);
			File toBeMergedFile = new File(toBeMergedDir,base);

			ResourceResults res = indexer.indexArc(arcFile);
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
		}
	}

	/**
	 * Add any new CDX files in toBeMergedDir to the BDB, deleting the CDX
	 * files as they are merged
	 * @param dbWriter
	 */
	public void mergeIndex(BDBResourceIndexWriter dbWriter) {
		int numMerged = 0;
		Iterator toBeMerged = getDirFilesIterator(toBeMergedDir);
		while(toBeMerged.hasNext()) {
			
			File indexFile = new File(toBeMergedDir,(String) toBeMerged.next());
			
			try {
				dbWriter.importFile(indexFile);
				if (!indexFile.delete()) {
					throw new IOException("Unable to unlink "
							+ indexFile.getAbsolutePath());
				}
				numMerged++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (numMerged > 0) {
			System.out.println("Merged " + numMerged + " files.");
		}
	}

	/**
	 * Gather a snapshot of the pipeline in a PipelineStatus object.
	 * @return PipelineStatus
	 */
	public PipelineStatus getStatus() {
		PipelineStatus status = new PipelineStatus();
		String index[] = toBeIndexedDir.list();
		String merge[] = toBeMergedDir.list();
		String numQueuedForIndex = (index == null) ? "0" : "" + index.length;
		String numQueuedForMerge = (merge == null) ? "0" : "" + merge.length;
		status.setNumQueuedForIndex(numQueuedForIndex);
		status.setNumQueuedForMerge(numQueuedForMerge);
		return status;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	/**
	 * Thread that repeatedly runs processing of an IndexPipeline and merges new
	 * data into a BDBResourceIndex
	 * 
	 * @author Brad Tofel
	 * @version $Date$, $Revision$
	 */
	private class IndexPipelineThread extends Thread {
		private final static int SLEEP_MILLISECONDS = 10000;

		private BDBResourceIndexWriter merger = null;
		private ArcIndexer indexer = new ArcIndexer();
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
			System.out.print("Pipeline Thread is ALIVE!");
		}

		public void run() {

			while (true) {
				try {
					pipeline.queueNewArcsForIndex();
					pipeline.indexArcs(indexer);
					pipeline.mergeIndex(merger);
					sleep(SLEEP_MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				// System.out.println("I'm running!"); catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
