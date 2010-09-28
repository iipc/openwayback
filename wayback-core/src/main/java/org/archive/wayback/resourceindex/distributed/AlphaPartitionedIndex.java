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
package org.archive.wayback.resourceindex.distributed;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.util.flatfile.FlatFile;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AlphaPartitionedIndex implements ResourceIndex {
	private static final Logger LOGGER =
        Logger.getLogger(AlphaPartitionedIndex.class.getName());


	/**
	 * config name for path where map file is found
	 */
	public static String RANGE_MAP_PATH = "resourceindex.distributed.mappath";
	/**
	 * config name for interval, in seconds, to check if the map file changed
	 */
	public static String RANGE_CHECK_INTERVAL =
		"resourceindex.distributed.checkinterval";
	private static long MS_PER_SEC = 1000;
	private static long DEFAULT_CHECK_INTERVAL = 100;
	
	private long lastLoadStat = 0;
	private long nextCheck = 0;
	private long checkInterval = DEFAULT_CHECK_INTERVAL;
	private RangeGroup groups[] = null;
	private String mapPath;
	private static Comparator<RangeGroup> comparator = 
		RangeGroup.getComparator();
	private UrlCanonicalizer canonicalizer = null;

	public AlphaPartitionedIndex() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}
	
	@SuppressWarnings("unchecked")
	private void reloadMapFile() throws IOException {
		FlatFile ff = new FlatFile(mapPath);
		Iterator itr = ff.getSequentialIterator();
		HashMap<String,RangeGroup> newGroupsMap = 
			new HashMap<String,RangeGroup>();
		HashMap<String,RangeGroup> oldGroupsMap = 
			new HashMap<String,RangeGroup>();
		
		if(groups != null) {
			for(int i = 0; i < groups.length; i++) {
				oldGroupsMap.put(groups[i].getName(),groups[i]);
			}
		}
		while(itr.hasNext()) {
			String line = (String) itr.next();
			String[] parts = line.split(" ");
			if(parts.length < 3) {
				throw new IOException("Unparseable map line(" + line + ")");
			}
			String name = parts[0];
			String start = parts[1];
			String end = parts[2];
			int numMembers = parts.length - 3;
			String[] members = new String[numMembers];
			for(int i = 0; i < numMembers; i++) {
				members[i] = parts[3 + i];
			}
			RangeGroup group = null;
			if(oldGroupsMap.containsKey(name)) {
				group = oldGroupsMap.get(name);
				if(start.compareTo(group.getStart()) != 0) {
					throw new IOException("Change of start range in " + 
							mapPath + " for range " + name);
				}
				if(end.compareTo(group.getEnd()) != 0) {
					throw new IOException("Change of end range in " + 
							mapPath + " for range " + name);
				}
			} else {
				group = new RangeGroup(name,start,end);
			}
			group.setMembers(members);
			newGroupsMap.put(name,group);
		}
		Collection<RangeGroup> c = newGroupsMap.values();
		RangeGroup[] newGroups = new RangeGroup[c.size()];
		Iterator itrg = c.iterator();
		for (int i=0; itrg.hasNext(); i++)
		    newGroups[i] = (RangeGroup) itrg.next();

//		RangeGroup[] newGroups = (RangeGroup[]) c.toArray();
		Arrays.sort(newGroups,comparator);
		groups = newGroups;
		LOGGER.info("Reloaded assignments from " + mapPath);
	}
	
	private void checkMapFile() throws IOException {
		long now = System.currentTimeMillis();
		if(nextCheck < now) {
			nextCheck = now + (checkInterval * MS_PER_SEC);
			File f = new File(mapPath);
			long curStat = f.lastModified();
			if(curStat > lastLoadStat) {

				reloadMapFile();
				lastLoadStat = curStat;
			}
		}
	}
	
	protected RangeGroup getRangeGroupForRequest(WaybackRequest wbRequest)
		throws BadQueryException, ResourceIndexNotAvailableException {
		
		String keyUrl;
		try {
			checkMapFile();
		} catch (IOException e) {
			// TODO: this is too much error info if we're repeatedly failing..
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException(e.getMessage());
		}

		if(groups == null || groups.length == 0) {
			throw new ResourceIndexNotAvailableException("empty map file");			
		}

	
		String searchUrl = wbRequest.getRequestUrl();
		if (searchUrl == null) {
			throw new BadQueryException("No " + WaybackRequest.REQUEST_URL 
					+ " specified");
		}

		try {
			keyUrl = canonicalizer.urlStringToKey(searchUrl);
		} catch (URIException e) {
			throw new BadQueryException("invalid "
					+ WaybackRequest.REQUEST_URL + " " + searchUrl);
		}
		RangeGroup dummy = new RangeGroup("",keyUrl,"");
		int loc = Arrays.binarySearch(groups,dummy,comparator);
		if(loc < 0) {
			loc = (loc * -1) - 2;
		}
		LOGGER.info("Using group(" + groups[loc].getName() + ") for url (" +
				keyUrl + ")");
		return groups[loc];
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceIndex#query(org.archive.wayback.core.WaybackRequest)
	 */
	public SearchResults query(WaybackRequest wbRequest)
		throws ResourceIndexNotAvailableException,
		ResourceNotInArchiveException, BadQueryException,
		AccessControlException {
		
		RangeGroup group = getRangeGroupForRequest(wbRequest);
		return group.query(wbRequest);
	}
	/**
	 * @param url
	 * @return canonicalized key version of url argument
	 * @throws URIException
	 */
	public String canonicalize(final String url) throws URIException {
		return canonicalizer.urlStringToKey(url);
	}

	/**
	 * @return the checkInterval
	 */
	public long getCheckInterval() {
		return checkInterval;
	}

	/**
	 * @param checkInterval the checkInterval to set
	 */
	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	/**
	 * @return the mapPath
	 */
	public String getMapPath() {
		return mapPath;
	}

	/**
	 * @param mapPath the mapPath to set
	 */
	public void setMapPath(String mapPath) {
		this.mapPath = mapPath;
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	public void shutdown() throws IOException {
		for(RangeGroup group : groups) {
			group.shutdown();
		}
	}
}
