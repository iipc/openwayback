package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.archive.wayback.ResourceStore;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.DirMaker;

/**
 * Class which implements a local ARC, WARC, ARC.gz, WARC.gz, ResourceStore
 * including an optional automatic indexing thread 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LocalResourceStore implements ResourceStore {

	private File dataDir = null;
	private AutoIndexThread indexThread = null;
	
	private ArcIndexer arcIndexer = new ArcIndexer();
	private WarcIndexer warcIndexer = new WarcIndexer();
	public final static String ARC_EXTENSION = ".arc";
	public final static String ARC_GZ_EXTENSION = ".arc.gz";
	public final static String WARC_EXTENSION = ".warc";
	public final static String WARC_GZ_EXTENSION = ".warc.gz";
	public final static String OPEN_EXTENSION = ".open";
	private final static String[] SUFFIXES = {
		"", ARC_EXTENSION, ARC_GZ_EXTENSION, WARC_EXTENSION, WARC_GZ_EXTENSION
	};
	private FilenameFilter filter = new ArcWarcFilenameFilter();
	
	public void init() throws ConfigurationException {
		if(indexThread != null) {
			indexThread.setStore(this);
			indexThread.start();
		}
	}
	protected String resultToFileName(SearchResult result) {
		return result.get(WaybackConstants.RESULT_ARC_FILE);
	}

	protected long resultToOffset(SearchResult result) {
		return Long.parseLong(result.get(WaybackConstants.RESULT_OFFSET));
	}
	
	public File getLocalFile(String fileName) {
		// try adding suffixes: empty string is first in the list
		File file = null;
		for(String suffix : SUFFIXES) {
			file = new File(dataDir,fileName + suffix);
			if(file.exists() && file.canRead()) {
				return file;
			}
		}
		// this might work if the full path is in the index...
		file = new File(fileName);
		if(file.exists() && file.canRead()) {
			return file;
		}
		// doh.
		return null;
	}
	
	public Resource retrieveResource(SearchResult result) throws IOException,
			ResourceNotAvailableException {
		String fileName = resultToFileName(result);
		long offset = resultToOffset(result);
		File file = getLocalFile(fileName);
		if (file == null) {
			
			// TODO: this needs to be prettied up for end user consumption..
			throw new ResourceNotAvailableException("Cannot find ARC file ("
					+ fileName + ")");
		} else {

			Resource r = ResourceFactory.getResource(file, offset);
			return r;
		}
	}
	
	public CloseableIterator<SearchResult> indexFile(File dataFile) throws IOException {
		CloseableIterator<SearchResult> itr = null;
		
		String name = dataFile.getName();
		if(name.endsWith(ARC_EXTENSION)) {
			itr = arcIndexer.iterator(dataFile);
		} else if(name.endsWith(ARC_GZ_EXTENSION)) {
			itr = arcIndexer.iterator(dataFile);			
		} else if(name.endsWith(WARC_EXTENSION)) {
			itr = warcIndexer.iterator(dataFile);
		} else if(name.endsWith(WARC_GZ_EXTENSION)) {
			itr = warcIndexer.iterator(dataFile);
		}		
		return itr;
	}

	public Iterator<String> fileNamesIterator() throws IOException {
		if(dataDir != null) {
			String[] files = dataDir.list(filter);
			List<String> l = Arrays.asList(files);
			return l.iterator();
		}
		return null;
	}
	
	public String getDataDir() {
		return DirMaker.getAbsolutePath(dataDir);
	}

	public void setDataDir(String dataDir) throws IOException {
		this.dataDir = DirMaker.ensureDir(dataDir);
	}
	
	private class ArcWarcFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			File tmp = new File(dir,name);
			if(tmp.isFile() && tmp.canRead()) {
				return name.endsWith(ARC_EXTENSION) ||
					name.endsWith(ARC_GZ_EXTENSION) ||
					name.endsWith(WARC_GZ_EXTENSION) ||
					name.endsWith(WARC_EXTENSION);
			}
			return false;
		}
	}

	public AutoIndexThread getIndexThread() {
		return indexThread;
	}
	public void setIndexThread(AutoIndexThread indexThread) {
		this.indexThread = indexThread;
	}
	public void shutdown() throws IOException {
		// no-op. could shut down threads
	}
}
