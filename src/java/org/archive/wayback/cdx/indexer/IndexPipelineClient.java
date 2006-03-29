/* IndexPipelineClient
 *
 * $Id$
 *
 * Created on 11:37:32 AM Mar 22, 2006.
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
package org.archive.wayback.cdx.indexer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.http11resourcestore.FileLocationDBClient;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class IndexPipelineClient {
	private static final Logger LOGGER = Logger.getLogger(IndexPipelineClient
			.class.getName());
	
	private final static String ARC_SUFFIX = ".arc.gz";
	private final static String CDX_SUFFIX = ".cdx";
	
	private String pipelineUrl = null;
	private HttpClient client = null;
	private File workDir = null;
	private ArcIndexer indexer = null; 
	private FileLocationDBClient locationClient = null;
	/**
	 * Create an IndexPipelineClient for adding ARC index information to a 
	 * remote index pipeline. Attempts to create workDir if it does not already
	 * exist.
	 * 
	 * @param pipelineUrl
	 * @param locationDBUrl 
	 * @param workDir
	 * @throws IOException
	 */
	public IndexPipelineClient(final String pipelineUrl, 
			final String locationDBUrl, final String workDir) 
	throws IOException {

		super();
		this.pipelineUrl = pipelineUrl;
		this.client = new HttpClient();
		this.workDir = new File(workDir);
		this.indexer = new ArcIndexer();
		this.locationClient = new FileLocationDBClient(locationDBUrl);
		
		if(!this.workDir.isDirectory()) {
			if(this.workDir.exists()) {
				throw new IOException("workDir path " + workDir +
						" exists but is not a directory");
			}
			if(!this.workDir.mkdirs()) {
				throw new IOException("Failed to mkdir(" + workDir + ")");
			}
		}
	}
	
	/**
	 * Inject File argument into the index pipeline specified for this client
	 * using HTTP PUT
	 * 
	 * @param cdx
	 * @throws HttpException
	 * @throws IOException
	 */
	public void uploadCDX(File cdx) throws HttpException, IOException {
		String basename = cdx.getName();
		String finalUrl = pipelineUrl + "/" + basename;
		PutMethod method = new PutMethod(finalUrl);
        method.setRequestEntity(new InputStreamRequestEntity(
        		new FileInputStream(cdx)));

		int statusCode = client.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Method failed: " + method.getStatusLine()
            		+ " for URL " + finalUrl + " on file " 
            		+ cdx.getAbsolutePath());
        }
        LOGGER.info("Uploaded cdx " + cdx.getAbsolutePath());
	}
	
	/**
	 * Create a CDX file for the arc argument, and add it to the remote
	 * index pipeline for this client.
	 * 
	 * @param arc
	 * @throws IOException 
	 */
	public void addArcToIndex(File arc) throws IOException {
		String arcBase = arc.getName();
		if(arcBase.endsWith(ARC_SUFFIX)) {
			arcBase = arcBase.substring(0,arcBase.length() - 
					ARC_SUFFIX.length());
		}
		String cdxBase = arcBase + CDX_SUFFIX;
		File tmpCDX = new File(workDir,cdxBase);
		LOGGER.info("Indexing arc " + arc.getAbsolutePath());
		SearchResults results = indexer.indexArc(arc);
		indexer.serializeResults(results, tmpCDX);
		uploadCDX(tmpCDX);
		if(!tmpCDX.delete()) {
			throw new IOException("Unable to unlink " + 
					tmpCDX.getAbsolutePath());
		}
	}
	
	/**
	 * Index each ARC in directory, upload CDX to the remote pipeline, and
	 * poke the remote locationDB to let it know where this ARC can be found.
	 * 
	 * @param directory
	 * @param httpPrefix
	 * @throws IOException 
	 */
	public void indexDirectory(File directory, String httpPrefix) 
	throws IOException {
		
		if(!httpPrefix.endsWith("/")) {
			httpPrefix += "/";
		}
		
		FileFilter filter = new FileFilter() {
			public boolean accept(File daFile) {
				return daFile.getName().endsWith(ARC_SUFFIX);
			}
		};

		File[] arcs = directory.listFiles(filter);
		if(arcs == null) {
			throw new IOException("Directory " + directory.getAbsolutePath() +
					" is not a directory or had an IO error");
		}
		for(int i = 0; i < arcs.length; i++) {
			File arc = arcs[i];
			String arcName = arc.getName();
			String arcUrl = httpPrefix + arcName;
			addArcToIndex(arc);
			LOGGER.info("Adding location " + arcUrl + " for arc " + arcName);
			locationClient.addArcUrl(arcName,arcUrl);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 5) {
			System.err.println("Usage: workDir pipelineUrl locationUrl arcDir arcUrlPrefix");
			return;
		}
		String workDir = args[0];
		String pipelineUrl = args[1];
		String locationUrl = args[2];
		File arcDir = new File(args[3]);
		String arcDirPrefix = args[4];
		IndexPipelineClient pipeClient;
		try {
			pipeClient = new IndexPipelineClient(pipelineUrl,
					locationUrl,workDir);
			pipeClient.indexDirectory(arcDir,arcDirPrefix);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
