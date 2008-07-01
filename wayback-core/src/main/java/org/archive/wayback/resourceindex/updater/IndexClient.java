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
package org.archive.wayback.resourceindex.updater;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.archive.wayback.core.CaptureSearchResult;
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
	
	private String target = null;
	private File tmpDir = null;
	
	private HttpClient client = new HttpClient();

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
				toBeMergedDir.mkdirs();
			}
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
	public boolean addSearchResults(String base, Iterator<CaptureSearchResult> itr) 
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
		
		Adapter<CaptureSearchResult,String> adapterSRtoS = 
			new SearchResultToCDXLineAdapter();
		Iterator<String> itrS = 
			new AdaptedIterator<CaptureSearchResult,String>(itr,adapterSRtoS);
		
		while(itrS.hasNext()) {
			pw.println(itrS.next());
		}
		pw.close();
		boolean added = addCDX(tmpFile);
		return added;
	}

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
		if(!this.tmpDir.isDirectory()) {
			this.tmpDir.mkdirs();
		}
	}
}
