package org.archive.wayback.resourcestore;

import java.io.FileNotFoundException;
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

public class FlexResourceStore implements ResourceStore {
	
	final static String[] EMPTY_STRINGS = new String[0];
	
	private final static Logger LOGGER = Logger.getLogger(FlexResourceStore.class.getName());
	
	protected ZipNumBlockLoader blockLoader;
	
	protected String customHeader;
	
	protected List<SourceResolver> sources;
	
	protected boolean failOnFirstUnavailable = false;
	
	public ZipNumBlockLoader getBlockLoader() {
		return blockLoader;
	}

	public void setBlockLoader(ZipNumBlockLoader blockLoader) {
		this.blockLoader = blockLoader;
	}

	public String getCustomHeader() {
		return customHeader;
	}

	public void setCustomHeader(String customHeader) {
		this.customHeader = customHeader;
	}

	public List<SourceResolver> getSources() {
		return sources;
	}

	public void setSources(List<SourceResolver> sources) {
		this.sources = sources;
	}

	public boolean isFailOnFirstUnavailable() {
		return failOnFirstUnavailable;
	}

	public void setFailOnFirstUnavailable(boolean failOnFirstUnavailable) {
		this.failOnFirstUnavailable = failOnFirstUnavailable;
	}

	public interface SourceResolver
	{
		String[] lookupPath(String filename) throws IOException;
	}
	
	public static class PathIndex implements SourceResolver
	{
		final static String DELIMITER = "\t";
		
		protected SortedTextFile pathIndex;
		protected String path;
		protected String prefixPath;
		
		public void setPathIndex(String path) throws IOException
		{
			this.path = path;
			this.pathIndex = new SortedTextFile(path, false);
		}
		
		public String getPathIndex()
		{
			return path;
		}
		
		public String getPrefixPath() {
			return prefixPath;
		}

		public void setPrefixPath(String prefixPath) {
			this.prefixPath = prefixPath;
		}

		@Override
		public String[] lookupPath(String filename) throws IOException {
			CloseableIterator<String> iter = null;
			List<String> paths = new ArrayList<String>();
			
			try {
				String prefix = filename + DELIMITER;
				
				iter = pathIndex.getRecordIterator(prefix);
				
				while (iter.hasNext()) {
					String line = iter.next();
					if (line.startsWith(prefix)) {
						String path = line.substring(prefix.length());
						if (prefixPath != null) {
							paths.add(prefixPath + path);
						} else {
							paths.add(path);
						}
					} else {
						break;
					}
				}
						
			} finally {
				if (iter != null) {
					try {
						iter.close();
					} catch (IOException e) {
						LOGGER.warning(e.toString());
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
		String skipPrefix = "http://";
		String includeFilter;
		
		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getSkipPrefix() {
			return skipPrefix;
		}

		public void setSkipPrefix(String skipPrefix) {
			this.skipPrefix = skipPrefix;
		}

		public String getIncludeFilter() {
			return includeFilter;
		}

		public void setIncludeFilter(String includeFilter) {
			this.includeFilter = includeFilter;
		}

		@Override
		public String[] lookupPath(String filename) {
			if (includeFilter != null) {
				if (!filename.contains(includeFilter)) {
					return EMPTY_STRINGS;
				}
			}
			
			if ((skipPrefix != null) && filename.startsWith(skipPrefix)) {
				return new String[]{filename};
			} else {
				return new String[]{prefix + filename};
			}
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
		boolean breakOnErr = false;
		
		StringBuilder excMsg = new StringBuilder();
		IOException lastExc = null;
				
		for (SourceResolver resolver : sources) {
			
			String[] paths = null;
			
			try {
				paths = resolver.lookupPath(filename);
			} catch (IOException io) {
				if (excMsg.length() > 0) {
					excMsg.append(" ");
				}
				excMsg.append(io.getMessage());
				lastExc = io;
				
				if (failOnFirstUnavailable) {
					breakOnErr = true;
					break;
				}
			}
			
			if (paths.length == 0) {
				continue;
			}
			
			for (String path : paths) {
				try {
					resource = getResource(path, result);
					
					if (resource != null) {
						return resource;
					}
					
				} catch (IOException io) {
					if (excMsg.length() > 0) {
						excMsg.append(" ");
					}
					excMsg.append(io.getMessage());
					lastExc = io;
					
					if (failOnFirstUnavailable) {
						breakOnErr = true;
						break;
					}
				}
			}
			
			if (breakOnErr) {
				break;
			}
		}
		
		if (lastExc == null) {
			lastExc = new FileNotFoundException(filename);
			excMsg.append("File not Found: " + filename);
		}

		ResourceNotAvailableException rnae = new ResourceNotAvailableException(excMsg.toString(), filename, lastExc);
		throw rnae;
	}
	
	public Resource getResource(String path, CaptureSearchResult result) throws IOException, ResourceNotAvailableException
	{		
		Resource r = null;
		
		long offset = result.getOffset();
		int length = (int)result.getCompressedLength();
		
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Loading " + path + " - " + offset + ":" + length);
		}
		
		boolean success = false;
		
		SeekableLineReader slr = blockLoader.attemptLoadBlock(path, offset, length, false, false);
		
		if (slr == null) {
			return null;
		}
		
		try {
			InputStream is = slr.getInputStream();
			
			r = loadResource(path, is);
			
			r.parseHeaders();
			
			success = true;
			
		} finally {
			if (!success) {
				if (slr != null) {
					slr.close();
				}	
			}
		}

		return r;
	}
	
	protected Resource loadResource(String path, InputStream is) throws IOException, ResourceNotAvailableException
	{
		ArchiveReader archiveReader = ArchiveReaderFactory.get(path, is, false);
		
		if (archiveReader instanceof ARCReader) {
			return new ArcResource((ARCRecord)archiveReader.get(), archiveReader);
		} else if (archiveReader instanceof WARCReader) {
			return new WarcResource((WARCRecord)archiveReader.get(), archiveReader);	
		} else {
			throw new IOException("Unknown ArchiveReader");
		}
	}

	@Override
	public void shutdown() throws IOException {
		blockLoader.close();
	}
}
