/* AlphaPartitionedIndex
 *
 * $Id$
 *
 * Created on 3:51:06 PM Jan 25, 2007.
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
import org.archive.wayback.WaybackConstants;
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
	
	@SuppressWarnings("unchecked")
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

	
		String searchUrl = wbRequest.get(WaybackConstants.REQUEST_URL);
		if (searchUrl == null) {
			throw new BadQueryException("No " + WaybackConstants.REQUEST_URL 
					+ " specified");
		}

		try {
			keyUrl = canonicalizer.urlStringToKey(searchUrl);
		} catch (URIException e) {
			throw new BadQueryException("invalid "
					+ WaybackConstants.REQUEST_URL + " " + searchUrl);
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
