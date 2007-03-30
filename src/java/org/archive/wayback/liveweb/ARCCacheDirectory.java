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
import java.util.Properties;
import java.util.logging.Logger;

import org.archive.io.ArchiveRecord;
import org.archive.io.WriterPoolSettings;
import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCWriter;
import org.archive.io.arc.ARCWriterPool;
import org.archive.wayback.PropertyConfigurable;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ConfigurationException;

/**
 * Class which manages a growing set of ARC files, managed by an ARCWriterPool.
 * 
 * Clients can grab an ARCWriter that they use to append to one of the ARC 
 * files.
 * 
 * This class also transforms ARCLocations into ARCRecords, using an ARCReader. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ARCCacheDirectory implements PropertyConfigurable {
	private static final Logger LOGGER = Logger.getLogger(
			ARCCacheDirectory.class.getName());

	private final static int MAX_POOL_WRITERS = 5;

	private final static int MAX_POOL_WAIT = 60 * 1000;

	private static final String OPEN_SUFFIX = ".open";
	/**
	 * directory where live generated ARCs are stored
	 */
	public static final String LIVE_WEB_ARC_DIR = "liveweb.arc.dir";

	/**
	 * prefeix for live generated ARC files.
	 */
	public static final String LIVE_WEB_ARC_PREFIX = "liveweb.arc.prefix";
	private ARCWriterPool pool = null;
	private File arcDir = null;

	private String getProp(Properties p, String key) 
		throws ConfigurationException {
	
		if(p.containsKey(key)) {
			String v = p.getProperty(key);
			if(v == null || v.length() < 1) {
				throw new ConfigurationException("Empty configuration " + key);				
			}
			return v;
		} else {
			throw new ConfigurationException("Missing configuration " + key);
		}

	}
	
	public void init(Properties p) throws ConfigurationException {
		String arcDirS = getProp(p,LIVE_WEB_ARC_DIR);
		arcDir = new File(arcDirS);
		if (!arcDir.isDirectory()) {
			if (arcDir.exists()) {
				throw new ConfigurationException("Path(" + arcDirS + ") " +
						"exists but is not a directory");
			}
			if(!arcDir.mkdirs()) {
				throw new ConfigurationException("Unable to mkdir(" + arcDirS +
						")");
			}
		}
		String arcPrefix = getProp(p,LIVE_WEB_ARC_PREFIX);
		File[] files = { arcDir };
		WriterPoolSettings settings = getSettings(true, arcPrefix, files);
		pool = new ARCWriterPool(settings, MAX_POOL_WRITERS, MAX_POOL_WAIT);
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
	 * @throws IOException
	 */
	public ARCWriter getWriter() throws IOException {
		return (ARCWriter) pool.borrowFile();
	}
	
	/**
	 * @param w previously borrowed ARCWriter
	 * @throws IOException
	 */
	public void returnWriter(ARCWriter w) throws IOException {
		pool.returnFile(w);
	}
	
	/**
	 * transform an ARCLocation into a Resource. Be sure to call close() on it
	 * when processing is finished.
	 * @param path 
	 * @param offset 
	 * @return the Resource for the location
	 * @throws IOException 
	 */
	public Resource getResource(String path, long offset) throws IOException {
		Resource resource = null;
		File arc = new File(path);
		if(!arc.exists()) {
			String base = arc.getName();
			arc = new File(arcDir,base);
			if(!arc.exists()) {
				if(base.endsWith(OPEN_SUFFIX)) {
					String newBase = base.substring(0,base.length() -
							OPEN_SUFFIX.length());
					arc = new File(arcDir,newBase);
				}
			}
		}

		LOGGER.info("Retrieving record at " + offset + " in " + 
				arc.getAbsolutePath());
		ARCReader reader = null;
		try {
			reader = ARCReaderFactory.get(arc,true,offset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		ArchiveRecord aRec = reader.get(offset);
		if(!(aRec instanceof ARCRecord)) {
			throw new IOException("Not ARCRecord...");
		}
		ARCRecord rec = (ARCRecord) aRec;
		resource = new Resource(rec,reader);
		return resource;
	}
	
	private WriterPoolSettings getSettings(final boolean isCompressed,
			final String prefix, final File[] arcDirs) {
		return new WriterPoolSettings() {
			public int getMaxSize() {
				return ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE;
			}

			public List getOutputDirs() {
				return Arrays.asList(arcDirs);
			}

			public boolean isCompressed() {
				return isCompressed;
			}

			public List getMetadata() {
				return null;
			}

			public String getPrefix() {
				return prefix;
			}

			public String getSuffix() {
				// TODO: is correct?
				return ARCConstants.DOT_ARC_FILE_EXTENSION;
			}
		};
	}
}
