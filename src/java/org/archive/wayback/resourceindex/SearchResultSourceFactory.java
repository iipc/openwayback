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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.archive.util.InetAddressUtil;
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

	private static String getRequiredValue(Properties p, String name, 
			String defaultValue) throws ConfigurationException {
		String value = p.getProperty(name);
		if(value == null) {
			if(defaultValue == null) {
				throw new ConfigurationException("Missing property " + name);
			}
			value = defaultValue;
		}
		return value;
	}
	
	/**
	 * @param p
	 * @return SearchResultSource as specified in Properties
	 * @throws ConfigurationException
	 */
	public static SearchResultSource get(Properties p) 
		throws ConfigurationException {
		
		SearchResultSource src = null;
		String className = getRequiredValue(p,SOURCE_CLASS,SOURCE_CLASS_BDB);
		if(className.equals(SOURCE_CLASS_BDB)) {
			src = getBDBIndex(p);
		} else if(className.equals(SOURCE_CLASS_CDX)) {
			src = getCDXIndex(p);
		} else if(className.equals(SOURCE_CLASS_DYNAMIC_CDX)) {
			src = getDynamicCDXIndex(p);
		} else {
			throw new ConfigurationException("Unknown " + SOURCE_CLASS + 
					" configuration, try one of: " + SOURCE_CLASS_BDB + ", " +
					SOURCE_CLASS_CDX);
		}
		return src;
	}
	
	private static SearchResultSource getCDXIndex(Properties p) 
		throws ConfigurationException {
		
		String pathString = getRequiredValue(p,CDX_PATHS,null);
		String paths[] = pathString.split(",");
		if(paths.length > 1) {
			CompositeSearchResultSource src = new CompositeSearchResultSource();
			for(int i = 0; i < paths.length; i++) {
				CDXIndex component = new CDXIndex(paths[i]);
				src.addSource(component);
			}
			return src;
		}
		return new CDXIndex(paths[0]);
	}

	// TODO: duplicated code -- refactor
	private static void ensureDir(File dir) throws ConfigurationException {
		if(!dir.isDirectory()) {
			if(dir.exists()) {
				throw new ConfigurationException("directory (" + 
						dir.getAbsolutePath() + 
						") exists but is not a directory.");
			}
			if(!dir.mkdirs()) {
				throw new ConfigurationException("unable to create directory(" + 
						dir.getAbsolutePath() + ")");
			}
		}
	}

	
	private static SearchResultSource getBDBIndex(Properties p) 
		throws ConfigurationException {
		
		BDBIndex index = new BDBIndex();
		String path = getRequiredValue(p,INDEX_PATH,null);
		String name = getRequiredValue(p,DB_NAME,"DB1");
		try {
			index.initializeDB(path,name);
		} catch (DatabaseException e) {
			throw new ConfigurationException(e.getMessage());
		}
		
		String incomingPath = getRequiredValue(p,INCOMING_PATH,"");
		if(incomingPath.length() > 0) {
			File incoming = new File(incomingPath);
			ensureDir(incoming);
			BDBIndexUpdater updater = new BDBIndexUpdater(index,incoming);

			String mergedPath = getRequiredValue(p,MERGED_PATH,"");
			String failedPath = getRequiredValue(p,FAILED_PATH,"");
			String mergeInterval = getRequiredValue(p,MERGE_INTERVAL,"");
			
			if(mergedPath.length() > 0) {
				File merged = new File(mergedPath);
				ensureDir(merged);
				updater.setMerged(merged);
			}
			if(failedPath.length() > 0) {
				File failed = new File(failedPath);
				ensureDir(failed);
				updater.setFailed(failed);
			}
			if(mergeInterval.length() > 0) {
				updater.setRunInterval(Integer.parseInt(mergeInterval));
			}
			updater.startup();
		}
		
		return index;
	}
	
	private static CachedFile makeCachedFile(String url, File dir, String name,
			long interval) throws MalformedURLException {
		return new CachedFile(new File(dir,name),new URL(url),interval);
	}
	
	private static SearchResultSource getDynamicCDXIndex(Properties p)
	throws ConfigurationException {

		String interval = getRequiredValue(p,CDX_INTERVAL,"10000");
		String dataDir = getRequiredValue(p,CDX_DIR,null);
		String rangeUrl = getRequiredValue(p,CDX_RANGE_URL,null);
		String definitionUrl = getRequiredValue(p,CDX_DEFINITION_URL,null);
		String md5Url = getRequiredValue(p,CDX_MD5_URL,null);
		String extraNodeName = getRequiredValue(p,CDX_NODE_NAME,"");
		List names = InetAddressUtil.getAllLocalHostNames();
		if(extraNodeName.length() > 0) {
			names.add(extraNodeName);
		}
		Object nodeNames[] = names.toArray();
		
		File dir = new File(dataDir);
		ensureDir(dir);
		
		long intL = Long.parseLong(interval);
		int intI = Integer.parseInt(interval);
		
		CachedFile rcf;
		CachedFile dcf;
		CachedFile mcf;
		try {
			rcf = makeCachedFile(rangeUrl,dir,"range.txt",intL);
			dcf = makeCachedFile(definitionUrl,dir,"definition.txt",intL);
			mcf = makeCachedFile(md5Url,dir,"md5.txt",intL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getLocalizedMessage());
		}
		
		RangeAssignmentFile rangeFile = new RangeAssignmentFile(rcf);
		CDXDefinitionFile cdxFile = new CDXDefinitionFile(dcf);
		MD5LocationFile md5File = new MD5LocationFile(mcf);
		
		return new DynamicCDXIndex(nodeNames,intI,dir,rangeFile,cdxFile,md5File);
	}
}
