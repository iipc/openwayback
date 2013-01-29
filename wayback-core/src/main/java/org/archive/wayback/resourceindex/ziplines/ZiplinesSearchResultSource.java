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
package org.archive.wayback.resourceindex.ziplines;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.SearchResultSource;
import org.archive.wayback.resourceindex.cdx.CDXFormatToSearchResultAdapter;
import org.archive.wayback.resourceindex.cdx.format.CDXFlexFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.flatfile.FlatFile;

/**
 * A set of Ziplines files, which are CDX files specially compressed into a 
 * series of GZipMembers such that:
 * 
 * 1) each member is exactly 128K, padded using a GZip comment header
 * 2) each member contains complete lines: no line spans two GZip members
 * 
 * If the data put into these files is sorted, then the data within the files
 * can be uncompressed when needed, minimizing the total data to be uncompressed
 * 
 * This SearchResultSource assumes a set of alphabetically partitioned Ziplined
 * CDX files, so that each file is sorted, and no regions overlap.
 * 
 * This class takes 2 files as input:
 * 1) a specially constructed map of the first N bytes of data from each GZip
 *      member, and the filename and offset of that GZip member.
 * 2) a mapping of filenames to URLs
 * 
 * Data from #1 is actually stored in a serialized 
 * 
 * 
 * 
 * @author brad
 * 
 * 
 * @ deprecated Note, this implementation is now superceded by the one in archive-commons
 * @see org.archive.format.gzip.zipnum.ZipNumCluster
 * That implementation provides support for stream loading of blocks, as well as summary files
 * from http and hdfs as well as local filesystem
 */

public class ZiplinesSearchResultSource implements SearchResultSource {
	private static final Logger LOGGER = Logger.getLogger(
			ZiplinesSearchResultSource.class.getName());

	/**
	 * Local path containing map of URL,TIMESTAMP,CHUNK,OFFSET for each 128K chunk
	 */
	private String chunkIndexPath = null;
	private FlatFile chunkIndex = null;
	/**
	 * Local path containing URL for each CHUNK
	 */
	private String chunkMapPath = null;
	private HashMap<String,BlockLocation> chunkMap = null;
	private CDXFormat format = null;
	private int maxBlocks = 1000;
	private BlockLoader blockLoader = null;
	
	protected int timestampDedupLength = 0;
	
	public int getTimestampDedupLength() {
		return timestampDedupLength;
	}
	public void setTimestampDedupLength(int timestampDedupLength) {
		this.timestampDedupLength = timestampDedupLength;
	}
	
	public ZiplinesSearchResultSource() {
	}
	public ZiplinesSearchResultSource(CDXFormat format) {
		this.format = format;
	}
	public void init() throws IOException {
		chunkMap = new HashMap<String, BlockLocation>();
		FlatFile ff = new FlatFile(chunkMapPath);
		CloseableIterator<String> lines = ff.getSequentialIterator();
		while(lines.hasNext()) {
			String line = lines.next();
			String[] parts = line.split("\\s");
			if(parts.length < 2) {
				LOGGER.severe("Bad line(" + line +") in (" + 
						chunkMapPath + ")");
				throw new IOException("Bad line(" + line +") in (" + 
						chunkMapPath + ")");
			}
			
			String locations[] = new String[parts.length - 1];
			for(int i = 1; i < parts.length; i++) {
				locations[i-1] = parts[i];
			}
			BlockLocation bl = new BlockLocation(parts[0], locations);
			chunkMap.put(parts[0],bl);
		}
		lines.close();
		chunkIndex = new FlatFile(chunkIndexPath);
	}
	protected CloseableIterator<CaptureSearchResult> adaptIterator(Iterator<String> itr) 
	throws IOException {
		return new AdaptedIterator<String,CaptureSearchResult>(itr,
				new CDXFormatToSearchResultAdapter(format));
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#cleanup(org.archive.wayback.util.CloseableIterator)
	 */
	public void cleanup(CloseableIterator<CaptureSearchResult> c)
			throws IOException {
		c.close();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator<CaptureSearchResult> getPrefixIterator(
			String prefix) throws ResourceIndexNotAvailableException {
		try {
			return adaptIterator(getStringPrefixIterator(prefix));
		} catch (IOException e) {
			throw new ResourceIndexNotAvailableException(e.getMessage());
		}
	}
	
	private String getTimestamp(String line)
	{
		if (timestampDedupLength <= 0) {
			return null;
		}
		
		int space = line.indexOf(' ');
		if (space >= 0) {
			return line.substring(0, space + 1 + timestampDedupLength);
		} else {
			return null;
		}
	}

	private ArrayList<ZiplinedBlock> getBlockListForPrefix(String prefix, String urlkey)
	throws IOException, ResourceIndexNotAvailableException {
		ArrayList<ZiplinedBlock> blocks = new ArrayList<ZiplinedBlock>();
		boolean first = true;
		int numBlocks = 0;
		boolean truncated = false;
		CloseableIterator<String> itr = null;
		
		
		try {
			itr = chunkIndex.getRecordIteratorLT(prefix);
			
			String currLine = null;
			String nextLine = null;
			
			if (itr.hasNext()) {
				nextLine = itr.next();
			}
			String timestamp = getTimestamp(nextLine);
			String lastTimestamp = null;
			
			while(nextLine != null) {
				
				currLine = nextLine;
				
				if (itr.hasNext()) {
					nextLine = itr.next();
				} else {
					nextLine = null;
				}
								
				if (nextLine != null && timestamp != null) {
					lastTimestamp = timestamp;
					timestamp = getTimestamp(nextLine);
					if ((timestamp != null) && timestamp.equals(lastTimestamp)) {
						continue;
					}
				}
				
				if(numBlocks >= maxBlocks || (!prefix.equals(urlkey) && numBlocks >= 1)) {
					if (LOGGER.isLoggable(Level.WARNING)) {
						LOGGER.warning("Truncated by blocks for " + prefix);
					}
					truncated = true;
					break;
				}
				
				numBlocks++;
				String blockDescriptor = currLine;
				String parts[] = blockDescriptor.split("\t");
				if((parts.length < 3)) {
					LOGGER.severe("Bad line(" + blockDescriptor +") in (" + 
							chunkMapPath + ")");
					throw new ResourceIndexNotAvailableException("Bad line(" + 
							blockDescriptor + ")");
				}
				// only compare the correct length:
				String prefCmp = urlkey;
				String blockCmp = parts[0];
				if(first) {
					// always add first:
					first = false;
				} else if(!blockCmp.startsWith(prefCmp)) {
					// all done;
					break;
				}
				// add this and keep lookin...
				BlockLocation bl = chunkMap.get(parts[1]);
				if(bl == null) {
					LOGGER.severe("No locations for block(" + parts[1] +")");
					throw new ResourceIndexNotAvailableException(
							"No locations for block(" + parts[1] + ")");
				}
				long offset = Long.parseLong(parts[2]);
				ZiplinedBlock block;
				if(parts.length == 3) {
					if (LOGGER.isLoggable(Level.INFO)) {
						LOGGER.info("Adding block source(" + parts[1] + "):" + offset);
					}
					block = new ZiplinedBlock(bl.getLocations(), offset);
				} else {
					int length = Integer.parseInt(parts[3]);
					if (LOGGER.isLoggable(Level.INFO)) {
						LOGGER.info("Adding block source(" + parts[1] + "):" + offset + " - " + length);
					}
					block = new ZiplinedBlock(bl.getLocations(), offset, length);
				}
				block.setLoader(blockLoader);
				blocks.add(block);
			}
		} finally {
			if(itr != null) {
				itr.close();
			}
		}
		return blocks;
	}
	
	public Iterator<String> getZiplinesChunkIterator(String prefix, String urlkey) throws ResourceIndexNotAvailableException, IOException
	{
		ArrayList<ZiplinedBlock> blocks = getBlockListForPrefix(prefix, urlkey);
		ZiplinesChunkIterator zci = new ZiplinesChunkIterator(blocks);
		zci.setTruncated(false);
		return zci;
	}
	
//	public Iterator<String> getMergedZiplinesChunkIterator(String prefix) throws ResourceIndexNotAvailableException, IOException
//	{
//		ArrayList<ZiplinedBlock> blocks = getMergedBlockListForPrefix(prefix);
//		ZiplinesChunkIterator zci = new ZiplinesChunkIterator(blocks);
//		zci.setTruncated(false);
//		return zci;
//	}
//	
//	public Iterator<String> getStringBoundedRangeIterator(String start, String end, boolean endInclusivePrefix) 
//	throws ResourceIndexNotAvailableException, IOException {
//		return new StringBoundedRangeIterator(getZiplinesChunkIterator(start), start, end, endInclusivePrefix);
//	}

	public Iterator<String> getStringPrefixIterator(String prefix) 
		throws ResourceIndexNotAvailableException, IOException {
		
		String urlkey = prefix;
		int space = prefix.indexOf(' ');
		if (space >= 0) {
			urlkey = prefix.substring(0, space);
		}
		
		return new StringPrefixIterator(getZiplinesChunkIterator(prefix, urlkey), urlkey);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixReverseIterator(java.lang.String)
	 */
	public CloseableIterator<CaptureSearchResult> getPrefixReverseIterator(
			String prefix) throws ResourceIndexNotAvailableException {
		throw new ResourceIndexNotAvailableException("unsupported op");
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#shutdown()
	 */
	public void shutdown() throws IOException {
		// no-op..
	}
	/**
	 * @return the format
	 */
	public CDXFormat getFormat() {
		return format;
	}
	/**
	 * @param format the format to set
	 */
	public void setFormat(CDXFormat format) {
		this.format = format;
	}
	/**
	 * @return the chunkIndexPath
	 */
	public String getChunkIndexPath() {
		return chunkIndexPath;
	}
	/**
	 * @param chunkIndexPath the chunkIndexPath to set
	 */
	public void setChunkIndexPath(String chunkIndexPath) {
		this.chunkIndexPath = chunkIndexPath;
	}
	/**
	 * @return the chunkMapPath
	 */
	public String getChunkMapPath() {
		return chunkMapPath;
	}
	/**
	 * @param chunkMapPath the chunkMapPath to set
	 */
	public void setChunkMapPath(String chunkMapPath) {
		this.chunkMapPath = chunkMapPath;
	}
	/**
	 * @return the maxBlocks
	 */
	public int getMaxBlocks() {
		return maxBlocks;
	}
	/**
	 * @param maxBlocks the maxBlocks to set
	 */
	public void setMaxBlocks(int maxBlocks) {
		this.maxBlocks = maxBlocks;
	}

	/**
	 * @return the blockLoader
	 */
	public BlockLoader getBlockLoader() {
		return blockLoader;
	}

	/**
	 * @param blockLoader the blockLoader to set
	 */
	public void setBlockLoader(BlockLoader blockLoader) {
		this.blockLoader = blockLoader;
	}

	private static void USAGE() {
		System.err.println("USAGE:");
		System.err.println("");
		System.err.println("zl-bin-search [-format FORMAT] [-max MAX_BLOCKS] SUMMARY LOCATION KEY");
		System.err.println("");
		System.err.println("Search a ziplined compressed CDX format index for key");
		System.err.println("KEY to STDOUT. SUMMARY and LOCATION are paths to the");
		System.err.println("block summary and file location files.");
		System.err.println("With -format, output CDX in format FORMAT.");
		System.err.println("With -max, limit search at most MAX_BLOCKS blocks.");
		System.exit(1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String cdxSpec = CDXFormatIndex.CDX_HEADER_MAGIC;
		String cdxSpec = " CDX N b a m s k r V g";
		CDXFormat format = null;
		BlockLoader blockLoader = new GenericBlockLoader();
		try {
			format = new CDXFormat(cdxSpec);
		} catch (CDXFormatException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		ZiplinesSearchResultSource zl = new ZiplinesSearchResultSource(format);
		PrintWriter pw = new PrintWriter(System.out);
		int idx;
		boolean blockDump = false;
		for(idx = 0; idx < args.length; idx++) {
			if(args[idx].equals("-format")) {
				idx++;
				if(idx >= args.length) {
					USAGE();
				}
				try {
					zl.setFormat(new CDXFormat(args[idx]));
				} catch (CDXFormatException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			} else if(args[idx].equals("-flexFormat")) {
				try {
					zl.setFormat(new CDXFlexFormat(" CDX A"));
				} catch (CDXFormatException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			} else if(args[idx].equals("-blockDump")) {
				blockDump = true;
			} else if(args[idx].equals("-hdfs")) {
				idx++;
				if(idx >= args.length) {
					USAGE();
				}
				blockLoader = new HDFSBlockLoader(args[idx]);
				try {
					((HDFSBlockLoader)blockLoader).init();
				} catch (IOException e) {
					e.printStackTrace();
					USAGE();
					System.exit(1);
				} catch (URISyntaxException e) {
					e.printStackTrace();
					USAGE();
					System.exit(1);
				}
			} else if(args[idx].equals("-max")) {
				idx++;
				if(idx >= args.length) {
					USAGE();
				}
				try {
					zl.setMaxBlocks(Integer.parseInt(args[idx]));
				} catch(NumberFormatException e) {
					USAGE();
					System.exit(1);
				}

			} else if(args[idx].equals("-debug")) {
				Logger.getLogger(
						ZiplinesSearchResultSource.class.getName()).setLevel(Level.ALL);
				Logger.getLogger(
						ZiplinesChunkIterator.class.getName()).setLevel(Level.ALL);
				Logger.getLogger(
						ZiplinedBlock.class.getName()).setLevel(Level.ALL);
				
			} else {
				break;
			}
		}
		if(args.length < idx + 3) {
			USAGE();
		}
		// first is summary path, then location path, then search key:
		zl.setBlockLoader(blockLoader);
		zl.setChunkIndexPath(args[idx++]);
		zl.setChunkMapPath(args[idx++]);
		String key = args[idx++];
		
		try {
			zl.init();
			if(blockDump) {
				
				ArrayList<ZiplinedBlock> blocks = zl.getBlockListForPrefix(key, key);
				for(ZiplinedBlock block : blocks) {
					pw.format("%s\t%s\n", block.urlOrPaths[0], block.offset);
				}
				pw.close();

			} else {
				Iterator<String> itr = zl.getStringPrefixIterator(key);
				boolean truncated = ((StringPrefixIterator)itr).isTruncated();
				while(itr.hasNext()) {
					pw.println(itr.next());
				}
				pw.close();
				if(truncated) {
					System.err.println("Note that results are truncated...");
				}
			}
		} catch (ResourceIndexNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
}
