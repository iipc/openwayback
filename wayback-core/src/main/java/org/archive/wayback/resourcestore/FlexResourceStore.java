package org.archive.wayback.resourcestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.format.gzip.zipnum.ZipNumBlockLoader;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCRecord;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;
import org.archive.wayback.webapp.PerformanceLogger;

public class FlexResourceStore implements ResourceStore {
	
	final static String[] EMPTY_STRINGS = new String[0];
	
	private final static Logger LOGGER = Logger.getLogger(FlexResourceStore.class.getName());
	
	protected ZipNumBlockLoader blockLoader;
	
	protected List<SourceResolver> sources;
	
	public ZipNumBlockLoader getBlockLoader() {
		return blockLoader;
	}

	public void setBlockLoader(ZipNumBlockLoader blockLoader) {
		this.blockLoader = blockLoader;
	}

	public List<SourceResolver> getSources() {
		return sources;
	}

	public void setSources(List<SourceResolver> sources) {
		this.sources = sources;
	}

	interface SourceResolver
	{
		String[] lookupPath(String filename) throws IOException;
	}
	
	public static class PathIndex extends SortedTextFile implements SourceResolver
	{
		final static String DELIMITER = "\t";
		
		public PathIndex(String path) throws IOException
		{
			super(path);
		}		
		
		@Override
		public String[] lookupPath(String filename) throws IOException {
			CloseableIterator<String> iter = null;
			List<String> paths = new ArrayList<String>();
			
			try {
				String prefix = filename + DELIMITER;
				
				iter = super.getRecordIterator(prefix);
				
				while (iter.hasNext()) {
					String line = iter.next();
					if (line.startsWith(prefix)) {
						paths.add(line.substring(prefix.length()));
					} else {
						break;
					}
				}			
				
			} finally {
				if (iter != null) {
					try {
						iter.close();
					} catch (IOException e) {
						//e.printStackTrace();
					}
				}
			}
			
			if (paths.isEmpty()) {
				return EMPTY_STRINGS;
			}
			
			String[] pathsArray = new String[paths.size()];
			return paths.toArray(pathsArray);
		}		
	}
	
	public static class PrefixLookup implements SourceResolver
	{		
		String prefix;
		String includeFilter;
		
		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getIncludeFilter() {
			return includeFilter;
		}

		public void setIncludeFilter(String includeFilter) {
			this.includeFilter = includeFilter;
		}

		@Override
		public String[] lookupPath(String filename) throws IOException {
			if (includeFilter != null) {
				if (!filename.contains(includeFilter)) {
					return EMPTY_STRINGS;
				}
			}
			
			return new String[]{prefix + filename};
		}
	}

	@Override
	public Resource retrieveResource(CaptureSearchResult result)
			throws ResourceNotAvailableException {
		
		String filename = result.getFile();

		if (filename == null || filename.isEmpty()) {
			throw new ResourceNotAvailableException("No ARC/WARC name in search result...", filename);
		}
		
		Resource resource = null;
		
		String excMsg = null;
		IOException lastExc = null;
				
		for (SourceResolver resolver : sources) {
			try {
				String[] paths = resolver.lookupPath(result.getFile());
				
				if (paths.length == 0) {
					continue;
				}
				
				for (String path : paths) {
					resource = getResource(path, result.getOffset(), (int)result.getCompressedLength());
					
					if (resource != null) {
						return resource;
					}
				}
			} catch (IOException e) {
				if (excMsg != null) {
					excMsg += " ";
					excMsg += e.toString();
				} else {
					excMsg = e.toString();
				}
				
				lastExc = e;
			}
		}

		ResourceNotAvailableException rnae = new ResourceNotAvailableException(excMsg, result.getFile(), lastExc);
		throw rnae;
	}
	
	public Resource getResource(String path, long offset, int length) throws IOException
	{
		long start = System.currentTimeMillis();
		Resource r = null;
		
		SeekableLineReader slr = null;
		
		try {
			slr = blockLoader.createBlockReader(path);
			
			InputStream is = slr.seekWithMaxRead(offset, false, length);
			
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Loading " + path + " - " + offset + ":" + length);
			}
					
			ArchiveReader archiveReader = ArchiveReaderFactory.get(path, is, (offset == 0));
			
			if (archiveReader instanceof ARCReader) {
				r = new ArcResource((ARCRecord)archiveReader.get(), archiveReader);
			} else if (archiveReader instanceof WARCReader) {
				r = new WarcResource((WARCRecord)archiveReader.get(), archiveReader);	
			} else {
				throw new IOException("Unknown ArchiveReader");
			}
			
			r.parseHeaders();
			
		} finally {
			if ((slr != null) && (r == null)) {
				try {
					slr.close();
				} catch (IOException io) {
					
				}
			}
		}
		
		long elapsed = System.currentTimeMillis() - start;
		PerformanceLogger.noteElapsed("{W}arcBlockLoader", elapsed, path);
		
		return r;
	}

	@Override
	public void shutdown() throws IOException {
		blockLoader.close();
	}
	
}
