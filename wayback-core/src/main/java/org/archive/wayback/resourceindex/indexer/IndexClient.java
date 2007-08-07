/* IndexClient
 *
 * $Id$
 *
 * Created on 4:22:52 PM Oct 12, 2006.
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
package org.archive.wayback.resourceindex.indexer;

//import java.io.BufferedOutputStream;
import java.io.File;
//import java.io.FileFilter;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
//import java.io.OutputStream;
//import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
//import org.archive.wayback.core.SearchResults;
//import org.archive.wayback.resourcestore.ArcIndexer;
//import org.archive.wayback.resourcestore.http.FileLocationDBClient;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.cdx.SearchResultToCDXLineAdapter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class IndexClient {
	private static final Logger LOGGER = Logger.getLogger(IndexClient
			.class.getName());
	
//	private final static String ARC_SUFFIX = ".arc";
//	private final static String ARC_GZ_SUFFIX = ".arc.gz";
//	private final static String CDX_SUFFIX = ".cdx";
	
	private String target = null;
	private File tmpDir = null;
	
//	private String submitUrl = null;
	private HttpClient client = new HttpClient();
//	private ArcIndexer indexer = null; 

	/**
	 * @param cdx
	 * @return true if CDX was added to local or remote index
	 * @throws HttpException
	 * @throws IOException
	 */
	public boolean addCDX(File cdx) throws HttpException, IOException {
		boolean added = false;
		if(target == null) {
			throw new IOException("No target set");
		}
		String base = cdx.getName();
		if(target.startsWith("http://")) {
			String finalUrl = target;
			if(target.endsWith("/")) {
				finalUrl = target + base;
			} else {
				finalUrl = target + "/" + base;
			}
			PutMethod method = new PutMethod(finalUrl);
	        method.setRequestEntity(new InputStreamRequestEntity(
	        		new FileInputStream(cdx)));

			int statusCode = client.executeMethod(method);
	        if (statusCode == HttpStatus.SC_OK) {
		        LOGGER.info("Uploaded cdx " + cdx.getAbsolutePath() + " to " +
		        		finalUrl);
				if(!cdx.delete()) {
					throw new IOException("FAILED delete " + 
							cdx.getAbsolutePath());
				}

		        added = true;
	        } else {
	            throw new IOException("Method failed: " + method.getStatusLine()
	            		+ " for URL " + finalUrl + " on file " 
	            		+ cdx.getAbsolutePath());
	        }
			
		} else {
			// assume a local directory:
			File toBeMergedDir = new File(target);
			if(!toBeMergedDir.exists()) {
				throw new IOException("Target " + target + " does not exist");
			}
			if(!toBeMergedDir.isDirectory()) {
				throw new IOException("Target " + target + " is not a dir");
			}
			if(!toBeMergedDir.canWrite()) {
				throw new IOException("Target " + target + " is not writable");
			}
			File toBeMergedFile = new File(toBeMergedDir,base);
			if(toBeMergedFile.exists()) {
				LOGGER.severe("WARNING: "+toBeMergedFile.getAbsolutePath() +
						"already exists!");
			} else {
				if(cdx.renameTo(toBeMergedFile)) {
					LOGGER.info("Queued " + toBeMergedFile.getAbsolutePath() + 
							" for merging.");
					added = true;
				} else {
					LOGGER.severe("FAILED rename("+cdx.getAbsolutePath()+
							") to ("+toBeMergedFile.getAbsolutePath()+")");
				}
			}
		}
		return added;
	}
	
	/**
	 * @param base
	 * @param itr
	 * @return true if data was added to local or remote index
	 * @throws HttpException
	 * @throws IOException
	 */
	public boolean addSearchResults(String base, Iterator<SearchResult> itr) 
	throws HttpException, IOException {
		
		if(tmpDir == null) {
			throw new IOException("No tmpDir argument");
		}
		File tmpFile = new File(tmpDir,base);
		if(tmpFile.exists()) {
			// TODO: is this safe?
			if(!tmpFile.delete()) {
				throw new IOException("Unable to remove tmp " + 
						tmpFile.getAbsolutePath());
			}
		}
		FileOutputStream os = new FileOutputStream(tmpFile);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		PrintWriter pw = new PrintWriter(bos);
		
		Adapter<SearchResult,String> adapterSRtoS = 
			new SearchResultToCDXLineAdapter();
		Iterator<String> itrS = 
			new AdaptedIterator<SearchResult,String>(itr,adapterSRtoS);
		
		while(itrS.hasNext()) {
			pw.println(itrS.next());
		}
		pw.close();
		boolean added = addCDX(tmpFile);
		return added;
	}
	
//	
//	/**
//	 * Inject File argument into the index pipeline specified for this client
//	 * using HTTP PUT
//	 * 
//	 * @param cdx
//	 * @throws HttpException
//	 * @throws IOException
//	 */
//	public void uploadCDX(File cdx) throws HttpException, IOException {
//		String basename = cdx.getName();
//		String finalUrl = submitUrl + "/" + basename;
//		PutMethod method = new PutMethod(finalUrl);
//        method.setRequestEntity(new InputStreamRequestEntity(
//        		new FileInputStream(cdx)));
//
//		int statusCode = client.executeMethod(method);
//        if (statusCode != HttpStatus.SC_OK) {
//            throw new IOException("Method failed: " + method.getStatusLine()
//            		+ " for URL " + finalUrl + " on file " 
//            		+ cdx.getAbsolutePath());
//        }
//        LOGGER.info("Uploaded cdx " + cdx.getAbsolutePath());
//	}
//	
//	/**
//	 * Create a CDX file for the arc argument, and add it to the remote
//	 * index pipeline for this client.
//	 * 
//	 * @param arc
//	 * @param workDir 
//	 * @throws IOException 
//	 */
//	public void addArcToIndex(File arc,File workDir) throws IOException {
//		String arcBase = arc.getName();
//		if(arcBase.endsWith(ARC_SUFFIX)) {
//			arcBase = arcBase.substring(0,arcBase.length() - 
//					ARC_SUFFIX.length());
//		}
//		String cdxBase = arcBase + CDX_SUFFIX;
//		File tmpCDX = new File(workDir,cdxBase);
//		LOGGER.info("Indexing arc " + arc.getAbsolutePath());
//		SearchResults results = indexer.indexArc(arc);
//		indexer.serializeResults(results, tmpCDX);
//		uploadCDX(tmpCDX);
//		if(!tmpCDX.delete()) {
//			throw new IOException("Unable to unlink " + 
//					tmpCDX.getAbsolutePath());
//		}
//	}
//	
//	/**
//	 * @param arc
//	 * @param os
//	 * @throws IOException
//	 */
//	public void dumpArcIndex(File arc, OutputStream os) throws IOException {
//		BufferedOutputStream bos = new BufferedOutputStream(os);
//		PrintWriter pw = new PrintWriter(bos);
//		SearchResults results = indexer.indexArc(arc);
//		indexer.serializeResults(results,pw);
//	}
//	
//	/**
//	 * Index each ARC in directory, upload CDX to the remote pipeline, and
//	 * poke the remote locationDB to let it know where this ARC can be found.
//	 * 
//	 * @param directory
//	 * @param httpPrefix
//	 * @param locationClient 
//	 * @param workDir 
//	 * @throws IOException 
//	 */
//	public void indexDirectory(File directory, String httpPrefix,
//			FileLocationDBClient locationClient, File workDir) 
//	throws IOException {
//		if(!workDir.isDirectory()) {
//			if(workDir.exists()) {
//				throw new IOException("workDir path " + 
//						workDir.getAbsolutePath() +	" exists but is not a " +
//								"directory");
//			}
//			if(!workDir.mkdirs()) {
//				throw new IOException("Failed to mkdir(" + 
//						workDir.getAbsolutePath() + ")");
//			}
//		}
//		
//		if(!httpPrefix.endsWith("/")) {
//			httpPrefix += "/";
//		}
//		
//		FileFilter filter = new FileFilter() {
//			public boolean accept(File daFile) {
//				return daFile.getName().endsWith(ARC_SUFFIX);
//			}
//		};
//
//		File[] arcs = directory.listFiles(filter);
//		if(arcs == null) {
//			throw new IOException("Directory " + directory.getAbsolutePath() +
//					" is not a directory or had an IO error");
//		}
//		for(int i = 0; i < arcs.length; i++) {
//			File arc = arcs[i];
//			String arcName = arc.getName();
//			String arcUrl = httpPrefix + arcName;
//			addArcToIndex(arc,workDir);
//			LOGGER.info("Adding location " + arcUrl + " for arc " + arcName);
//			locationClient.addArcUrl(arcName,arcUrl);
//		}
//	}
//	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		if(args.length == 1) {
//			File arc = new File(args[0]);
//			ArcIndexer indexer = new ArcIndexer();
//			
//			BufferedOutputStream bos = new BufferedOutputStream(System.out);
//			PrintWriter pw = new PrintWriter(bos);
//			SearchResults results;
//			try {
//				results = indexer.indexArc(arc);
//				indexer.serializeResults(results,pw);
//			} catch (IOException e) {
//				e.printStackTrace();
//				System.exit(1);
//			}
//			return;
//		} else if(args.length != 5) {
//			System.err.println("Usage: workDir pipelineUrl locationUrl arcDir arcUrlPrefix");
//			System.err.println("Usage: arcPath");
//			return;
//		}
//		File workDir = new File(args[0]);
//		String pipelineUrl = args[1];
//		String locationUrl = args[2];
//		File arcDir = new File(args[3]);
//		String arcDirPrefix = args[4];
//		IndexClient pipeClient;
//		FileLocationDBClient locClient = new FileLocationDBClient(locationUrl);
//		try {
//			pipeClient = new IndexClient(pipelineUrl);
//			pipeClient.indexDirectory(arcDir,arcDirPrefix,locClient,workDir);
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the tmpDir
	 */
	public String getTmpDir() {
		if(tmpDir == null) {
			return null;
		}
		return tmpDir.getAbsolutePath();
	}

	/**
	 * @param tmpDir the tmpDir to set
	 */
	public void setTmpDir(String tmpDir) {
		this.tmpDir = new File(tmpDir);
	}
}
