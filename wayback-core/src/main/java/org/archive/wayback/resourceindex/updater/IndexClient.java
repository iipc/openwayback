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
package org.archive.wayback.resourceindex.updater;

import java.io.BufferedOutputStream;
import java.io.File;
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
				LOGGER.warning("WARNING: "+toBeMergedFile.getAbsolutePath() +
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
