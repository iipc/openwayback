/* RoboCache
 *
 * $Id$
 *
 * Created on 5:26:13 PM Feb 13, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCLocation;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCWriter;
import org.archive.io.arc.ARCWriterPool;
import org.archive.io.ArchiveRecord;
import org.archive.io.WriterPoolSettings;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.exception.ConfigurationException;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RoboCache implements ExclusionAuthority {
	private static final Logger LOGGER = Logger.getLogger(RoboCache.class
			.getName());

	private static final String ROBOT_DB_PATH = "liveweb.cachedbpath";

	private static final String ROBOT_DB_NAME = "liveweb.cachedbname";

	private static final String ROBOT_ARC_DIR = "liveweb.arcdir";

	private static final String ROBOT_ARC_PREFIX = "liveweb.arcprefix";

	private static final String TMP_DIR = "liveweb.tempdir";

	private final static String valueDelimiterRE = " ";

	private final static long MAX_CACHE_MS = 86400 * 1000;

	private final static int MAX_POOL_WRITERS = 5;

	private final static int MAX_POOL_WAIT = 60 * 1000;


	private UrlCacher urlCacher = null;

	/**
	 * Maximum BDBJE file size
	 */
	private final static String JE_LOG_FILEMAX = "256000000";

	/**
	 * path to directory containing the BDBJE files
	 */
	private String path;

	/**
	 * name of BDBJE db within the path directory
	 */
	private String dbName;

	/**
	 * BDBJE Environment
	 */
	Environment env = null;

	/**
	 * BDBJE Database
	 */
	Database db = null;

	ARCWriterPool pool = null;

	public void init(Properties p) throws ConfigurationException {
		String tmpPath = (String) p.get(TMP_DIR);
		if ((tmpPath == null) || (tmpPath.length() < 1)) {
			throw new ConfigurationException("Failed to find " + TMP_DIR);
		}
		String arcPath = (String) p.get(ROBOT_ARC_DIR);
		if ((arcPath == null) || (arcPath.length() < 1)) {
			throw new ConfigurationException("Failed to find " + ROBOT_ARC_DIR);
		}
		String arcPrefix = (String) p.get(ROBOT_ARC_PREFIX);
		if ((arcPrefix == null) || (arcPrefix.length() < 1)) {
			throw new ConfigurationException("Failed to find " +
					ROBOT_ARC_PREFIX);
		}
		String dbPath = (String) p.get(ROBOT_DB_PATH);
		if ((dbPath == null) || (dbPath.length() < 1)) {
			throw new ConfigurationException("Failed to find " + ROBOT_DB_PATH);
		}
		String dbName = (String) p.get(ROBOT_DB_NAME);
		if ((dbName == null) || (dbName.length() < 1)) {
			throw new ConfigurationException("Failed to find " + ROBOT_DB_NAME);
		}

		try {
			LOGGER.info("Initializing RobotCacheDB at(" + dbPath + ") named("
					+ dbName + ")");

			initializeDB(dbPath, dbName);
			initializeARCWriterPool(new File(arcPath), arcPrefix);
			initializeRoboPuller(new File(tmpPath));

		} catch (DatabaseException e) {
			// TODO one or the other...
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
	}

	/**
	 * @param thePath Directory where BDBJE files are stored
	 * @param theDbName Name of files in thePath
	 * @throws DatabaseException
	 */
	protected void initializeDB(final String thePath, final String theDbName)
			throws DatabaseException {
		path = thePath;
		dbName = theDbName;

		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);
		environmentConfig.setTransactional(true); // autocommit
		environmentConfig.setConfigParam("je.log.fileMax", JE_LOG_FILEMAX);
		File file = new File(path);
		if(!file.isDirectory()) {
			if(!file.mkdirs()) {
				throw new DatabaseException("failed mkdirs(" + path + ")");
			}
		}
		env = new Environment(file, environmentConfig);
		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setAllowCreate(true);
		databaseConfig.setTransactional(true); // autocommit
		// perform other database configurations

		db = env.openDatabase(null, dbName, databaseConfig);
	}

	protected void initializeARCWriterPool(File arcDir, String prefix) {
		if (!arcDir.isDirectory()) {
			if (arcDir.exists()) {
				throw new RuntimeException("Path(" + arcDir.getAbsolutePath()
						+ ") exists but is not a directory");
			}
			arcDir.mkdirs();
		}
		File[] files = { arcDir };
		boolean compress = true;
		WriterPoolSettings settings = getSettings(compress, prefix, files);
		pool = new ARCWriterPool(settings, MAX_POOL_WRITERS, MAX_POOL_WAIT);
	}

	protected void initializeRoboPuller(File tmpDir) throws IOException {
		urlCacher = new UrlCacher(tmpDir);
	}

	/**
	 * destroy this and sub objects "cleanly"
	 * 
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public void shutdown() throws DatabaseException, IOException {
		shutdownDB();
		shutdownARCWriter();
		shutdownRoboPuller();
	}

	/**
	 * shut down the BDB.
	 * 
	 * @throws DatabaseException
	 */
	public void shutdownDB() throws DatabaseException {

		if (db != null) {
			db.close();
		}

		if (env != null) {
			env.close();
		}
	}

	/**
	 * shut down the ARCWriter.
	 * 
	 * @throws IOException
	 */
	public void shutdownARCWriter() throws IOException {

		if (pool != null) {
			pool.close();
		}
	}

	private void shutdownRoboPuller() {

	}

	/**
	 * @return Returns the dbName.
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get latest robots.txt document from cache (adding to cache if needed)
	 * parse it, and check if the urlString requested is blocked for userAgent.
	 * 
	 * @param userAgent
	 * @param urlString
	 * @param timestamp 
	 * @return ExclusionResponse with answer to the query
	 * @throws DatabaseException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public ExclusionResponse checkRobotExclusion(String userAgent, String urlString,
			String timestamp)
			throws DatabaseException, MalformedURLException, IOException {

		URL url = null;
		url = new URL(ArchiveUtils.addImpliedHttpIfNecessary(urlString));

		// is there a valid cached copy?
		ARCLocation location = null;
		location = getCachedRobots(url);
		
		if (location == null) {
			// we don't have one cached. Try to get one into the cache:
			location = cacheFreshRobots(url);
		}
		
		// how about now?
		if (location == null) {
			LOGGER.info("FAILED to get for(" + urlString+ ") -- Allowed");
			return new ExclusionResponse(url.getHost(),
					ExclusionResponse.EXLCUSION_NON_AUTHORITATIVE,
					ExclusionResponse.EXLCUSION_AUTHORIZED,
					"Unable to retrieve robots.txt document");
		}
		if (location.getOffset() == 0) {
			// special value for FAILED get:
			LOGGER.info("Have FAILED get Cached for(" + urlString + ") -- Allowed");
			return new ExclusionResponse(url.getHost(),
					ExclusionResponse.EXLCUSION_NON_AUTHORITATIVE,
					ExclusionResponse.EXLCUSION_AUTHORIZED,
					"Failed retrieve cached document...");
		}

		// we have a local copy in an ARC file, extract and parse it:
		String pathToCheck = url.getPath();
		HashMap disallows = getDisallowsForUA(location, userAgent);
		if(disallowsBlockPath(disallows, userAgent, pathToCheck)) {
			return new ExclusionResponse(url.getHost(),
					ExclusionResponse.EXLCUSION_AUTHORITATIVE,
					ExclusionResponse.EXLCUSION_NOT_AUTHORIZED,
					"Access blocked by robots.txt");
		} else {
			return new ExclusionResponse(url.getHost(),
					ExclusionResponse.EXLCUSION_AUTHORITATIVE,
					ExclusionResponse.EXLCUSION_AUTHORIZED,
					"Access granted by robots.txt");
		}
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.accesscontrol.ExclusionAuthority#checkExclusion(java.lang.String, java.lang.String, java.lang.String)
	 */
	public ExclusionResponse checkExclusion(String userAgent, String urlString, 
			String captureDate) throws Exception {
		try {
			return checkRobotExclusion(userAgent,urlString,captureDate);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new Exception(e);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new Exception(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}
	
	private boolean disallowsBlockPath(HashMap disallows, String userAgent,
			String pathToCheck) {
		if (disallows == null) {
			// hrmm. broken disallows excludes nothing? 
			return false;
		}
		Iterator itr = disallows.keySet().iterator();
		while (itr.hasNext()) {
			String uaWithDisallows = (String) itr.next();
			if (uaWithDisallows.length() == 0
					|| uaWithDisallows.equals(userAgent)) {
				Iterator disItr = ((List) disallows.get(uaWithDisallows))
						.iterator();
				while (disItr.hasNext()) {
					String disallowed = (String) disItr.next();
					if (disallowed.length() == 0) {

						System.out.println("UA(" + uaWithDisallows
								+ ") has empty disallow: Go for it!");
						return false;

					} else {
						System.out.println("UA(" + uaWithDisallows + ") has ("
								+ disallowed + ") blocked...("
								+ disallowed.length() + ")");
						if (pathToCheck.startsWith(disallowed)) {
							System.out.println("THIS APPLIES!!!");
							return true;
						}
					}
				}
			} else {
				//System.out.println("Skipping irrelevant UA(" + uaWithDisallows
				//		+ ")");
			}
		}
		return false;
	}

	/**
	 * clear any cached value for host described by urlString, right now.
	 * 
	 * @param urlString
	 * @return ExclusionResponse object
	 * @throws MalformedURLException
	 * @throws DatabaseException
	 */
	public ExclusionResponse purgeUrl(String urlString) throws MalformedURLException, DatabaseException {
		ExclusionResponse eclResponse = null;
		URL url = null;
		url = new URL(urlString);

		DatabaseEntry key = new DatabaseEntry();

		String hostname = url.getHost();

		key.setData(hostname.getBytes());
		key.setPartial(false);
		OperationStatus status = db.delete(null,key);
		if((status != OperationStatus.NOTFOUND) 
				&& (status != OperationStatus.SUCCESS)) {
					throw new DatabaseException("Failed to delete " + hostname);
		}
		eclResponse = new ExclusionResponse(hostname,"PurgeConfirmation",true);
		LOGGER.info("Purged robots.txt for " + hostname);
		return eclResponse;
	}
	
	private HashMap getDisallowsForUA(ARCLocation location, String userAgent)
			throws MalformedURLException, IOException {

		ARCReader reader = ARCReaderFactory.get(new File(location.getName()),
				true, location.getOffset());

		ArchiveRecord aRec = reader.get(location.getOffset());
		if(!(aRec instanceof ARCRecord)) {
			throw new IOException("Not ARCRecord...");
		}
		ARCRecord rec = (ARCRecord) aRec;
		rec.skipHttpHeader();
		LinkedList userAgents = new LinkedList();
		userAgents.add(userAgent);
		HashMap disallows = new HashMap();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(InputStream) rec));
		boolean errors = Robotstxt.parse(br, userAgents, disallows);
		br.close();
		reader.close();
		if (errors) {
			return null;
		}
		return disallows;
	}

	private ARCLocation getCachedRobots(URL url) throws DatabaseException {
		ARCLocation found = null;
		String[] valueFields = null;

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();

		String hostname = url.getHost();

		key.setData(hostname.getBytes());
		key.setPartial(false);
		OperationStatus status = db.get(null,key,value,LockMode.DEFAULT);

		if (status == OperationStatus.SUCCESS) {
			String valueString = new String(value.getData());
			if (valueString != null && valueString.length() > 0) {
				valueFields = valueString.split(valueDelimiterRE);
				if (valueFields.length == 3) {
					long captureDate = Long.parseLong(valueFields[0]);
					final long cachedOffset = Long.parseLong(valueFields[1]);
					String arcPath = valueFields[2];
					Date now = new Date();
					LOGGER.info("Using Cached for (" + url.toString() + ") at("
							+ cachedOffset + ") in (" + arcPath + ")");

					// is it fresh enough?					
					if (now.getTime() - captureDate < MAX_CACHE_MS) {
						
						// possibly (probably) the file was named .open
						// when we first added the document. Perhaps it's been 
						// closed since: 
						if (arcPath.endsWith(".open")) {
							File tmp = new File(arcPath);
							if (!tmp.exists()) {
								arcPath = arcPath.substring(0,
										arcPath.length() - 5);
							}
						}
						final String cachedArcPath = arcPath;
						found = new ARCLocation() {
							private String filename = cachedArcPath;

							private long offset = cachedOffset;

							public String getName() {
								return this.filename;
							}

							public long getOffset() {
								return this.offset;
							}
						};
					}
				}
			}
		}

		return found;
	}

	private ARCLocation cacheFreshRobots(URL url) throws DatabaseException {
		ARCLocation fresh;
		ARCWriter writer;
		try {
			writer = (ARCWriter) pool.borrowFile();
			writer.checkSize();
		} catch (IOException e) {
			// TODO better...
			e.printStackTrace();
			return null;
		}
		String robotUrlString = "http://" + url.getHost() + "/robots.txt";
		fresh = urlCacher.cache(writer, robotUrlString);
		try {
			pool.returnFile(writer);
		} catch (IOException e) {
			// TODO better....
			e.printStackTrace();
			return null;
		}
		// add to cache:
		String hostname = url.getHost();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();

		key.setData(hostname.getBytes());
		key.setPartial(false);
		Date now = new Date();
		String newValue = "";
		if (fresh != null) {
			newValue = String.valueOf(now.getTime()) + valueDelimiterRE
					+ fresh.getOffset() + valueDelimiterRE + fresh.getName();
			LOGGER.info("Cached fresh for (" + url.toString() + ") at("
					+ fresh.getOffset() + ") in (" + fresh.getName() + ")");
		} else {
			newValue = String.valueOf(now.getTime()) + valueDelimiterRE + "0"
					+ valueDelimiterRE + "0";
			LOGGER.info("Recording FAILED GETfor (" + url.toString() + ")");
		}

		value.setData(newValue.getBytes());
		OperationStatus status = db.put(null,key,value);

		if (status != OperationStatus.SUCCESS) {
			throw new DatabaseException("oops, put had non-success status");
		}
		return fresh;
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
