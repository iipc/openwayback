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

	interface SourceResolver
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
				String[] paths = resolver.lookupPath(filename);
				
				if (paths.length == 0) {
					continue;
				}
				
				for (String path : paths) {
					resource = getResource(path, result);
					
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
				
				if (failOnFirstUnavailable) {
					break;
				}
			}
		}
		
		if (lastExc == null) {
			lastExc = new FileNotFoundException(filename);
			excMsg = "File not Found: " + filename;
		}

		ResourceNotAvailableException rnae = new ResourceNotAvailableException(excMsg, filename, lastExc);
		throw rnae;
	}
	
	public Resource getResource(String path, CaptureSearchResult result) throws IOException
	{		
		Resource r = null;
		
		long offset = result.getOffset();
		int length = (int)result.getCompressedLength();
		
		SeekableLineReader slr = null;
		
		try {
			slr = blockLoader.createBlockReader(path);
			
			slr.seekWithMaxRead(offset, false, length);
			
			InputStream is = slr.getInputStream();
			
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
			
			// Ability to pass on header prefix from data store request
//			if ((customHeader != null) && (slr instanceof HTTPSeekableLineReader)) {
//				HTTPSeekableLineReader httpSlr = (HTTPSeekableLineReader)slr;
//				String value = httpSlr.getHeaderValue(customHeader);
//				
//				if (value != null) {				
//					result.put(CaptureSearchResult.CUSTOM_HEADER_PREFIX + customHeader, value);
//				}
//			}
			
		} catch (Exception e) {
			
			if (LOGGER.isLoggable(Level.WARNING)) {
				LOGGER.warning(e.toString());
			}
			
			if (slr != null) {
				slr.close();
			}
			
			r = null;
			slr = null;
			
			if (e instanceof IOException) {
				throw (IOException)e;
			} else {
				throw new IOException(e);
			}
		}
				
		return r;
	}

	@Override
	public void shutdown() throws IOException {
		blockLoader.close();
	}
}
