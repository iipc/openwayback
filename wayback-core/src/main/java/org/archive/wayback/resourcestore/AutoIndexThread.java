package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.indexer.IndexClient;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.DirMaker;

/**
 * Thread that repeatedly notices new files in the LocalResourceStore, indexes
 * those files, and hands them off to a ResourceIndex via an IndexClient
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AutoIndexThread extends Thread {
	private static final Logger LOGGER =
        Logger.getLogger(AutoIndexThread.class.getName());

	private final static int DEFAULT_RUN_INTERVAL_MS = 10000;
	private LocalResourceStore store = null;
	private File workDir = null;
	private File queuedDir = null;
	private int runInterval = DEFAULT_RUN_INTERVAL_MS;
	private IndexClient indexClient = null;

	/**
	 * @param store
	 * @param runInterval
	 */
	public AutoIndexThread() {
		super("AutoARCIndexThread");
		super.setDaemon(true);
	}

	public void run() {
		LOGGER.info("AutoIndexThread is alive.");
		int sleepInterval = runInterval;
		if(store == null) {
			throw new RuntimeException("No LocalResourceStore set");
		}
		while (true) {
			try {
				int numIndexed = indexNewArcs();
				if (numIndexed == 0) {
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

	/**
	 * Scan for new ARC files, and index any new files discovered.
	 * 
	 * There are 3 main steps, which could be broken into separate threads:
	 * 1) detect new ARCs
	 * 2) create CDX files for each new ARC
	 * 3) upload CDX files to target (or rename to local "incoming" directory)
	 * 
	 * for now these are sequential.
	 * 
	 * @return number of ARC files indexed
	 */
	public int indexNewArcs() {
		int numIndexed = 0;
		try {
			queueNewArcsForIndex();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			numIndexed = indexArcs(10);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return numIndexed;
	}	
	/**
	 * Find any new ARC files and queue them for indexing. 
	 * @throws IOException
	 */
	public void queueNewArcsForIndex() throws IOException {
		
		// build a HashMap of what has been queued already:
		HashMap<String,String> queued = new HashMap<String, String>();
		String entries[] = queuedDir.list();
		if(entries != null) {
			for (int i = 0; i < entries.length; i++) {
				queued.put(entries[i], "i");
			}
		}
		// now scan thru arcDir, and make a flag file for anything that was not
		// already there:
		Iterator<String> files = store.fileNamesIterator();
		if(files != null) {
			while(files.hasNext()) {
				String fileName = files.next();
				if(!queued.containsKey(fileName)) {
					File newQueuedFile = new File(queuedDir,fileName);
					File newToBeIndexedFile = new File(workDir,fileName);
					newToBeIndexedFile.createNewFile();
					newQueuedFile.createNewFile();
				}
			}
		}
	}

	private String fileNameToBase(final String fileName) {
		return fileName;
	}
	
	/**
	 * Index up to 'max' ARC/WARC files queued for indexing, queueing the 
	 * resulting CDX files for merging with the BDBIndex.
	 * 
	 * @param indexer
	 * @param max maximum number to index in this method call, 0 for unlimited
	 * @return int number of ARC/WARC files indexed
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public int indexArcs(int max) 
	throws MalformedURLException, IOException {

		int numIndexed = 0;
		String toBeIndexed[] = workDir.list();
		
		if (toBeIndexed != null) {
			for (int i = 0; i < toBeIndexed.length; i++) {
				String fileName = toBeIndexed[i];
				File file = store.getLocalFile(fileName);
				if(file != null) {
					File workFlagFile = new File(workDir,fileName);
					String cdxBase = fileNameToBase(fileName);

					try {
					
						LOGGER.info("Indexing " + file.getAbsolutePath());
						CloseableIterator<SearchResult> itr = store.indexFile(file);
					
						if(indexClient.addSearchResults(cdxBase, itr)) {
							if (!workFlagFile.delete()) {
								throw new IOException("Unable to delete "
										+ workFlagFile.getAbsolutePath());
							}
						}
						itr.close();
						numIndexed++;
					} catch (IOException e) {
						LOGGER.severe("FAILED index: " + file.getAbsolutePath() 
								+ " cause: " + e.getLocalizedMessage());
					}
					if(max > 0 && (numIndexed >= max)) {
						break;
					}
				}
			}
		}
		return numIndexed;
	}
	

	
	public LocalResourceStore getStore() {
		return store;
	}

	public void setStore(LocalResourceStore store) {
		this.store = store;
	}

	public String getWorkDir() {
		return workDir == null ? null : workDir.getAbsolutePath();
	}

	public void setWorkDir(String workDir) throws IOException {
		this.workDir = DirMaker.ensureDir(workDir);
	}

	public String getQueuedDir() {
		return queuedDir == null ? null : queuedDir.getAbsolutePath();
	}

	public void setQueuedDir(String queuedDir) throws IOException {
		this.queuedDir = DirMaker.ensureDir(queuedDir);
	}

	public int getRunInterval() {
		return runInterval;
	}

	public void setRunInterval(int runInterval) {
		this.runInterval = runInterval;
	}

	public IndexClient getIndexClient() {
		return indexClient;
	}

	public void setIndexClient(IndexClient indexClient) {
		this.indexClient = indexClient;
	}
}
