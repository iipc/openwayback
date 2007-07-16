/* SearchResultSourceFactory
 *
 * $Id$
 *
 * Created on 5:25:49 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.archive.util.InetAddressUtil;
import org.archive.wayback.core.PropertyConfiguration;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.resourceindex.bdb.BDBIndex;
import org.archive.wayback.resourceindex.bdb.BDBIndexUpdater;
import org.archive.wayback.resourceindex.cdx.CDXIndex;
import org.archive.wayback.resourceindex.cdx.dynamic.CDXDefinitionFile;
import org.archive.wayback.resourceindex.cdx.dynamic.DynamicCDXIndex;
import org.archive.wayback.resourceindex.cdx.dynamic.MD5LocationFile;
import org.archive.wayback.resourceindex.cdx.dynamic.RangeAssignmentFile;
import org.archive.wayback.util.CachedFile;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResultSourceFactory {

	/**
	 * configuration name for implementor of SearchResultSource
	 */
	public final static String SOURCE_CLASS = "resourceindex.sourceclass";

	/**
	 * indicates one or more CDX files implementing SearchResultSource
	 */
	public final static String SOURCE_CLASS_CDX = "CDX";

	/**
	 * indicates a BDB implementing SearchResultSource
	 */
	public final static String SOURCE_CLASS_BDB = "BDB";

	/**
	 * indicates a dynamic set of  CDX files implementing SearchResultSource
	 */
	public final static String SOURCE_CLASS_DYNAMIC_CDX = "DYNACDX";

	/**
	 * configuration name for CDX Paths
	 */
	private final static String CDX_PATHS = "resourceindex.cdxpaths";
	
	/**
	 * configuration name for directory containing index
	 */
	public final static String INDEX_PATH = "resourceindex.indexpath";

	/**
	 * configuration name for directory containing new CDX files to merge into
	 * a BDB SearchResultSource. If specified, then a thread will be started
	 * that scans this directory, and any file in this directory will be
	 * treated as a CDX file containing records to be added to the index.
	 */
	public final static String INCOMING_PATH = "resourceindex.incomingpath";

	/**
	 * configuration name for directory where CDX files that were successfully
	 * added to the BDB index are placed. Ignored if INCOMING_PATH is not also
	 * set. If this value is not set, and INCOMING_PATH is set, then files
	 * successfully indexed will be deleted after they are indexed.
	 */
	private final static String MERGED_PATH = "resourceindex.mergedpath";

	/**
	 * configuration name for directory where CDX files that failed to parse
	 * are placed. If this value is not set, and INCOMING_PATH is set, then 
	 * files which do not parse are left in INCOMING_PATH, and will be 
	 * repeatedly attepted.
	 */
	private final static String FAILED_PATH = "resourceindex.failedpath";

	/**
	 * configuration name for number of milliseconds between scans of 
	 * INCOMING_PATH.
	 */
	private final static String MERGE_INTERVAL = "resourceindex.mergeinterval";

	/**
	 * configuration name for BDBJE database name within the db directory
	 */
	public final static String DB_NAME = "resourceindex.dbname";

	private final static String CDX_INTERVAL = "resourceindex.cdxinterval";
	private final static String CDX_NODE_NAME = "resourceindex.nodename";
	private final static String CDX_DIR = "resourceindex.cdxdir";
	private final static String CDX_RANGE_URL = "resourceindex.cdxrangeurl";
	private final static String CDX_DEFINITION_URL = "resourceindex.cdxdefnurl";
	private final static String CDX_MD5_URL = "resourceindex.cdxmd5url";

	/**
	 * @param p
	 * @return SearchResultSource as specified in Properties
	 * @throws ConfigurationException
	 */
	public static SearchResultSource get(Properties p) 
		throws ConfigurationException {
		
		SearchResultSource src = null;
		PropertyConfiguration pc = new PropertyConfiguration(p);
		String className = pc.getString(SOURCE_CLASS,SOURCE_CLASS_BDB);
		if(className.equals(SOURCE_CLASS_BDB)) {
			src = getBDBIndex(pc);
		} else if(className.equals(SOURCE_CLASS_CDX)) {
			src = getCDXIndex(pc);
		} else if(className.equals(SOURCE_CLASS_DYNAMIC_CDX)) {
			src = getDynamicCDXIndex(pc);
		} else {
			throw new ConfigurationException("Unknown " + SOURCE_CLASS + 
					" configuration, try one of: " + SOURCE_CLASS_BDB + ", " +
					SOURCE_CLASS_CDX);
		}
		return src;
	}
	
	private static SearchResultSource getCDXIndex(PropertyConfiguration pc) 
		throws ConfigurationException {
		
		String pathString = pc.getString(CDX_PATHS);
		String paths[] = pathString.split(",");
		if(paths.length > 1) {
			CompositeSearchResultSource src = new CompositeSearchResultSource();
			for(int i = 0; i < paths.length; i++) {
				CDXIndex component = new CDXIndex();
				component.setPath(paths[i]);
				src.addSource(component);
			}
			return src;
		}
		CDXIndex index = new CDXIndex();
		index.setPath(paths[0]);
		return index;
	}

	private static SearchResultSource getBDBIndex(PropertyConfiguration pc) 
		throws ConfigurationException {
		
		BDBIndex index = new BDBIndex();
		String path = pc.getString(INDEX_PATH);
		String name = pc.getString(DB_NAME,"DB1");
		try {
			index.initializeDB(path,name);
		} catch (DatabaseException e) {
			throw new ConfigurationException(e.getMessage());
		}
		
		String incomingPath = pc.getString(INCOMING_PATH, "");
		if(incomingPath.length() > 0) {
			File incoming = pc.getDir(INCOMING_PATH,true);

			BDBIndexUpdater updater = new BDBIndexUpdater(index,incoming);
			try {
			if(pc.getString(MERGED_PATH,"").length() > 0) {
				updater.setMerged(pc.getDir(MERGED_PATH,true));
			}
			if(pc.getString(FAILED_PATH,"").length() > 0) {
				updater.setFailed(pc.getDir(FAILED_PATH,true));
			}
			} catch(IOException e) {
				throw new ConfigurationException(e.getLocalizedMessage());
			}
			if(pc.getString(MERGE_INTERVAL,"").length() > 0) {
				updater.setRunInterval(pc.getInt(MERGE_INTERVAL));
			}
			updater.startup();
		}
		
		return index;
	}
	
	private static CachedFile makeCachedFile(String url, File dir, String name,
			long interval) throws MalformedURLException {
		return new CachedFile(new File(dir,name),new URL(url),interval);
	}
	
	private static SearchResultSource getDynamicCDXIndex(PropertyConfiguration 
			pc) throws ConfigurationException {

		int intInterval = pc.getInt(CDX_INTERVAL, 10000);
		long longInterval = pc.getLong(CDX_INTERVAL,10000);
		File dataDir = pc.getDir(CDX_DIR,true);
		
		
		String rangeUrl = pc.getString(CDX_RANGE_URL);
		String definitionUrl = pc.getString(CDX_DEFINITION_URL);
		String md5Url = pc.getString(CDX_MD5_URL);
		String extraNodeName = pc.getString(CDX_NODE_NAME,"");
		List<String> names = InetAddressUtil.getAllLocalHostNames();
		if(extraNodeName.length() > 0) {
			names.add(extraNodeName);
		}
		Object nodeNames[] = names.toArray();
		
		
		CachedFile rcf;
		CachedFile dcf;
		CachedFile mcf;
		try {
			rcf = makeCachedFile(rangeUrl,dataDir,"range.txt",longInterval);
			dcf = makeCachedFile(definitionUrl,dataDir,"definition.txt",
					longInterval);
			mcf = makeCachedFile(md5Url,dataDir,"md5.txt",longInterval);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getLocalizedMessage());
		}
		
		RangeAssignmentFile rangeFile = new RangeAssignmentFile(rcf);
		CDXDefinitionFile cdxFile = new CDXDefinitionFile(dcf);
		MD5LocationFile md5File = new MD5LocationFile(mcf);
		
		return new DynamicCDXIndex(nodeNames,intInterval,dataDir,rangeFile,
				cdxFile,md5File);
	}
}
