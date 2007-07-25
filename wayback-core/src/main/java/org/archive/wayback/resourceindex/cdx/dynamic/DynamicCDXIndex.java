/* DynamicCDXIndex
 *
 * $Id$
 *
 * Created on 3:41:41 PM Jan 25, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.cdx.dynamic;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.FileDownloader;
import org.archive.wayback.resourceindex.CompositeSearchResultSource;
import org.archive.wayback.resourceindex.cdx.CDXIndex;
import org.archive.wayback.resourceindex.cdx.dynamic.CDXDefinitionFile;
import org.archive.wayback.resourceindex.cdx.dynamic.MD5LocationFile;
import org.archive.wayback.resourceindex.cdx.dynamic.RangeAssignmentFile;

/**
 * A CompositeSearchResultSource that autmatically manages it's list of sources
 * based on 3 configuration files, and a background thread:
 * Config 1: Mapping of ranges to hosts responsible for that range
 *               this class is aware of the local host name, so uses this file
 *               to determin which range(s) should be local
 *
 * Config 2: Mapping of ranges to one or more MD5s that compose that range
 *               when all of these MD5s have been copied local, this index 
 *               becomes active, and each request uses a composite of these
 *               local files
 *
 * Config 3: Mapping of MD5s to locations from which they can be retrieved
 *               when a file that should be local is missing, these locations
 *               will be used to retrieve a copy of that file
 * 
 * Background Thread: compares current set of files to the various 
 *               configurations files, gets files local that need to be and
 *               updates the composite set searched when the correct set of
 *               MD5s are localized. 
 *
 * The Thread maintains the state of the sychronization with the desired file
 * set:
 *   UNKNOWN: If the desired state is unknown
 *   SYNCHING: If the local state does not match the desired state
 *   SYNCHED: If the local stat matches the desired state
 * 
 * This class forwards all method requests to the superclass, if the state is
 * SYNCHED, otherwise throws a ResourceIndexNotAvailableException.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class DynamicCDXIndex extends CompositeSearchResultSource {

	private static final Logger LOGGER =
        Logger.getLogger(DynamicCDXIndex.class.getName());

	protected static int STATE_UNKNOWN = 0;
	protected static int STATE_SYNCHING = 1;
	protected static int STATE_SYNCHED = 2;
	
	private int state = STATE_UNKNOWN;
	private File dataDir;
	private static Thread syncherThread = null;
	protected static String MD5_PATTERN = "^[0-9a-f_.-]{32}$";
    protected static final Pattern MD5_REGEX = Pattern.compile(MD5_PATTERN);
	
	/**
	 * @param nodeNames 
	 * @param interval
	 * @param dataDir 
	 * @param rangeFile
	 * @param definitionFile
	 * @param md5File
	 */
	public DynamicCDXIndex(Object nodeNames[], int interval, File dataDir,
			RangeAssignmentFile rangeFile, CDXDefinitionFile definitionFile,
			MD5LocationFile md5File) {
		super();
		this.dataDir = dataDir;
		startUpThread(nodeNames,interval,rangeFile,definitionFile,md5File);
	}
	
	protected Object[] getLocalMD5s() {
		return dataDir.list(new md5FilenameFilter());
	}

	protected File dataFileForMD5(String md5) {
		return new File(dataDir,md5);
	}
	
	protected void setCDXFiles(Object md5s[]) {
		sources.clear();
		for(int i = 0; i< md5s.length; i++) {
			File cdx = dataFileForMD5((String) md5s[i]);
			CDXIndex index = new CDXIndex();
			index.setPath(cdx.getAbsolutePath());
			addSource(index);
		}
	}
	
	private synchronized void startUpThread(Object nodeNames[], int interval,
			RangeAssignmentFile rangeFile, CDXDefinitionFile definitionFile,
			MD5LocationFile md5File) {
		if (syncherThread != null) {
			return;
		}
		syncherThread = new DynamicCDXSyncherThread(this, nodeNames, interval,
				rangeFile, definitionFile,md5File);
		syncherThread.start();
		
	}

	protected synchronized void setState(int newState) {
		state = newState;
	}

	protected synchronized int getState() {
		return state;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator<SearchResult> getPrefixIterator(String prefix)
			throws ResourceIndexNotAvailableException {
		if(getState() != STATE_SYNCHED) {
			throw new ResourceIndexNotAvailableException("Not synchronized");
		}
		return super.getPrefixIterator(prefix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixReverseIterator(java.lang.String)
	 */
	public CloseableIterator<SearchResult> getPrefixReverseIterator(String prefix)
			throws ResourceIndexNotAvailableException {

		if(getState() != STATE_SYNCHED) {
			throw new ResourceIndexNotAvailableException("Not synchronized");
		}
		return super.getPrefixReverseIterator(prefix);
	}

	private class md5FilenameFilter implements FilenameFilter {

		/* (non-Javadoc)
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File dir, String name) {
			return name.matches(MD5_PATTERN);
		}
	}
	
	private class DynamicCDXSyncherThread extends Thread {
		private RangeAssignmentFile rangeFile = null;
		private CDXDefinitionFile definitionFile = null;
		private MD5LocationFile md5File = null;
		private DynamicCDXIndex index = null;

		private Object nodeNames[];
		private int runInterval;
		private FileDownloader downloader;

		/**
		 * @param index
		 * @param nodeNames 
		 * @param runInterval
		 * @param rangeFile
		 * @param definitionFile
		 * @param md5File
		 */
		public DynamicCDXSyncherThread(DynamicCDXIndex index, Object nodeNames[],
				int runInterval, RangeAssignmentFile rangeFile, 
				CDXDefinitionFile definitionFile, MD5LocationFile md5File ) {
			
			super("DynamicCDXSyncherThread");
			super.setDaemon(true);
			this.index = index;
			this.nodeNames = nodeNames;
			this.runInterval = runInterval;
			this.rangeFile = rangeFile;
			this.definitionFile = definitionFile;
			this.md5File = md5File;
			this.downloader = new FileDownloader();
			this.downloader.setDigest(true);
			LOGGER.info("DynamicCDXSyncherThread is alive.");
		}
		
		private Object[] getDesiredMD5s() throws IOException {
			ArrayList<String> allRanges = new ArrayList<String>();
			for(int i = 0; i < nodeNames.length; i++) {
				Object ranges[] = rangeFile.getRangesForNode((String) nodeNames[i]);
				for(int j=0; j<ranges.length; j++) {
					allRanges.add((String)ranges[j]);
				}
			}
			Object ranges[] = allRanges.toArray();
			ArrayList<String> md5sNeeded = new ArrayList<String>();
			for(int i = 0; i < ranges.length; i++) {
				Object rangeMD5s[] = definitionFile.getMD5sForRange((String) ranges[i]);
				for(int j=0; j < rangeMD5s.length; j++) {
					md5sNeeded.add((String)rangeMD5s[j]);
				}
			}
			return md5sNeeded.toArray();
		}

		private Object[] getCurrentMD5s() {
			return index.getLocalMD5s();
		}

		private void removeFiles(Object toBeRemoved[]) throws IOException {
			for(int i=0; i< toBeRemoved.length; i++) {
				File toDelete = index.dataFileForMD5((String) toBeRemoved[i]);
				if(!toDelete.delete()) {
					throw new IOException("Failed to remove " +
							toDelete.getAbsolutePath());
				}
			}
		}
		
		private void downloadFiles(Object toBeDownloaded[]) throws IOException {
			for(int i=0; i< toBeDownloaded.length; i++) {
				String neededMD5 = (String) toBeDownloaded[i];
				File target = index.dataFileForMD5(neededMD5);
				File tmpTarget = new File(target.getAbsolutePath() + ".TMP");
				Object locs[] = md5File.getLocationsForMD5(neededMD5);
				boolean gotFile = false;
				for(int j=0; j< locs.length; j++) {
					String loc = (String) locs[j];
					URL u = new URL(loc);
					try {
						if(loc.endsWith(".gz")) {
							downloader.downloadGZ(u,tmpTarget);
						} else {
							downloader.download(u,tmpTarget);
						}
						String gotMD5 = downloader.getLastDigest();
						if(gotMD5.equals(neededMD5)) {
							gotFile = true;
							tmpTarget.renameTo(target);
							break;
						} else {
							tmpTarget.delete();
							LOGGER.warning("Bad file contents. Location(" + 
									loc +") should have MD5(" + neededMD5 +
									") but has MD5(" + gotMD5 +")");
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
				}
				if(!gotFile) {
					throw new IOException("Unable to get MD5 " +
							neededMD5);
				}
			}			
		}
		
		public void run() {
			int sleepInterval = runInterval;
			boolean setInitial = false;
			while (true) {
				try {
					// get desired index state:
					Object desired[] = getDesiredMD5s();
					// get current index state:
					Object current[] = getCurrentMD5s();

					// work to do?
					HashMap<String,Object> desiredMap = 
						new HashMap<String, Object>();
					ArrayList<String> extra = new ArrayList<String>();
					for(int i=0; i< desired.length; i++) {
						desiredMap.put((String)desired[i],null);
					}
					for(int i=0; i< current.length; i++) {
						if(desiredMap.containsKey(current[i])) {
							desiredMap.remove(current[i]);
						} else {
							extra.add((String)current[i]);
						}
					}
					Set<String> needed = desiredMap.keySet();
					if((needed.size() + extra.size()) > 0) {
						// whoops -- we're off:
						index.setState(DynamicCDXIndex.STATE_SYNCHING);

						// first remove extras:
						removeFiles(extra.toArray());
						
						// now get needed:
						downloadFiles(needed.toArray());

						index.setCDXFiles(desired);
						index.setState(DynamicCDXIndex.STATE_SYNCHED);
					} else if(!setInitial && current.length > 0) {
						index.setCDXFiles(desired);
						index.setState(DynamicCDXIndex.STATE_SYNCHED);
					}
					sleep(sleepInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}