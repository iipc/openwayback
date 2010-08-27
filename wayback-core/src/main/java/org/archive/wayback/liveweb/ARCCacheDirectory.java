/* ARCCacheDirectory
 *
 * $Id$
 *
 * Created on 6:15:25 PM Mar 13, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.liveweb;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.archive.io.ArchiveFileConstants;
import org.archive.io.WriterPoolSettings;
import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCWriter;
import org.archive.io.arc.ARCWriterPool;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;
import org.archive.wayback.util.DirMaker;

/**
 * Class uses an ARCWriterPool to provide access to a set of ARCWriters, 
 * exposing getting and setters which simplify Spring configuration.
 * 
 * This class also provides one method, getResource() which transforms a String
 * path and an long offset into a Resource.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ARCCacheDirectory {
	private static final Logger LOGGER = Logger.getLogger(
			ARCCacheDirectory.class.getName());

	private int poolWriters = 5;
	private int maxPoolWait = 5 * 1000;	
	private long maxARCSize = ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE;
	private String arcPrefix = "wayback-live";
	
	/**
	 * template string used to configure the ARC writer pool
	 */
	public static String LIVE_WAYBACK_TEMPLATE = 
		"${prefix}-${timestamp17}-${serialno}";
	
	
	private File arcDir = null;
	private ARCWriterPool pool = null;

	/**
	 * @throws IOException for usual reasons
	 */
	public void init() throws IOException {
		// TODO: check that all props have been set
		arcDir = DirMaker.ensureDir(arcDir.getAbsolutePath(),"arcPath");
		File[] files = { arcDir };
		WriterPoolSettings settings = getSettings(true, arcPrefix, files);
		pool = new ARCWriterPool(settings, poolWriters, maxPoolWait);
	}
	
	/**
	 * shut down the ARC Writer pool.
	 */
	public void shutdown() {
		pool.close();
	}
	
	/**
	 * get an ARCWriter. be sure to return it to the pool with returnWriter.
	 * 
	 * @return an ARCWriter prepared to store an ARCRecord
	 * @throws IOException for usual reasons
	 */
	public ARCWriter getWriter() throws IOException {
		return (ARCWriter) pool.borrowFile();
	}
	
	/**
	 * @param w previously borrowed ARCWriter
	 * @throws IOException for usual reasons
	 */
	public void returnWriter(ARCWriter w) throws IOException {
		pool.returnFile(w);
	}
	
	/**
	 * transform an ARCLocation into a Resource. Be sure to call close() on it
	 * when processing is finished.
	 * @param path to ARC file
	 * @param offset within file where record begins
	 * @return the Resource for the location
	 * @throws IOException for usual reasons
	 */
	public Resource getResource(String path, long offset) throws IOException {
		File arc = new File(path);
		if(!arc.exists()) {
			String base = arc.getName();
			arc = new File(arcDir,base);
			if(!arc.exists()) {
				if(base.endsWith(ArchiveFileConstants.OCCUPIED_SUFFIX)) {
					String newBase = base.substring(0,base.length() -
							ArchiveFileConstants.OCCUPIED_SUFFIX.length());
					arc = new File(arcDir,newBase);
				}
			}
		}
		LOGGER.info("Retrieving record at " + offset + " in " + 
				arc.getAbsolutePath()); 
		try {
			return ResourceFactory.getResource(arc, offset);
		} catch (ResourceNotAvailableException e1) {
			throw new IOException(e1.getMessage());
		}
	}
	
	private WriterPoolSettings getSettings(final boolean isCompressed,
			final String prefix, final File[] arcDirs) {
		return new WriterPoolSettings() {
			public List<File> getOutputDirs() {
				return Arrays.asList(arcDirs);
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public List getMetadata() {
				return null;
			}

			public String getPrefix() {
				return prefix;
			}

			public boolean getCompress() {
				// TODO Auto-generated method stub
				return isCompressed;
			}

			public long getMaxFileSizeBytes() {
				// TODO Auto-generated method stub
				return maxARCSize;
			}

			public String getTemplate() {
				return LIVE_WAYBACK_TEMPLATE;
			}
		};
	}

	/**
	 * @return the arcPrefix
	 */
	public String getArcPrefix() {
		return arcPrefix;
	}

	/**
	 * @param arcPrefix the arcPrefix to set
	 */
	public void setArcPrefix(String arcPrefix) {
		this.arcPrefix = arcPrefix;
	}

	/**
	 * @return the arcDir
	 */
	public String getArcDir() {
		return arcDir.getAbsolutePath();
	}

	/**
	 * @param arcPath the arcPath to set
	 */
	public void setArcDir(String arcPath) {
		this.arcDir = new File(arcPath);
	}

	/**
	 * @return the poolWriters
	 */
	public int getPoolWriters() {
		return poolWriters;
	}

	/**
	 * @param poolWriters the poolWriters to set
	 */
	public void setPoolWriters(int poolWriters) {
		this.poolWriters = poolWriters;
	}

	/**
	 * @return the maxPoolWait
	 */
	public int getMaxPoolWait() {
		return maxPoolWait;
	}

	/**
	 * @param maxPoolWait the maxPoolWait to set
	 */
	public void setMaxPoolWait(int maxPoolWait) {
		this.maxPoolWait = maxPoolWait;
	}

	/**
	 * @return the maxARCSize
	 */
	public long getMaxARCSize() {
		return maxARCSize;
	}

	/**
	 * @param maxARCSize the maxARCSize to set
	 */
	public void setMaxARCSize(long maxARCSize) {
		this.maxARCSize = maxARCSize;
	}
}
