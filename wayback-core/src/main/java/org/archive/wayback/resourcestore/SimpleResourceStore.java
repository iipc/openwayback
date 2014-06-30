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
package org.archive.wayback.resourcestore;

import java.io.IOException;
import java.util.logging.Logger;

import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ArcWarcFilenameFilter;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;


/**
 * Implements ResourceStore where ARC/WARCs are accessed via a local file or an
 * HTTP 1.1 range request. All files are assumed to be "rooted" at a particular
 * HTTP URL, or within a single local directory. The HTTP version may imply a
 * file reverse-proxy to connect through to actual HTTP ARC/WARC locations.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SimpleResourceStore implements ResourceStore {

	private final static Logger LOGGER = Logger.getLogger(
			SimpleResourceStore.class.getName());
	private static String HTTP_ERROR = "HTTP";
	private static String HTTP_502 = "502";
	private String prefix = null;
	private String regex;
	private String replace;
	private String includeFilter;
  
	private int retries = 2;

	public Resource retrieveResource(CaptureSearchResult result)
		throws ResourceNotAvailableException {

		// extract ARC filename
		String fileName = result.getFile();
		if(fileName == null || fileName.length() < 1) {
			throw new ResourceNotAvailableException("No ARC/WARC name in search result...", fileName);
		}
		
        // If includeFilter is provided, filter out paths that don't contain the include filter
        if (includeFilter != null) {
        	if (!fileName.contains(includeFilter)) {
        		throw new ResourceNotAvailableException("Resource " + fileName + " not found in this store", fileName);
        	}
        }		

		final long offset = result.getOffset();
		if(!fileName.endsWith(ArcWarcFilenameFilter.ARC_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.ARC_GZ_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.WARC_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.WARC_GZ_SUFFIX)) {
			fileName = fileName + ArcWarcFilenameFilter.ARC_GZ_SUFFIX;
		}
				
        String fileUrl;
        if ( regex != null && replace != null )
          {
            fileUrl = fileName.replaceAll( regex, replace );
          }
        else
          {
            fileUrl = prefix + fileName;
          }
        
		Resource r = null;
		try {
			int attempts = retries;
	        while(attempts-- > 0) {
	        	try {
	        		r = ResourceFactory.getResource(fileUrl, offset);
	        		break;
	        	} catch (IOException e) {
	        		String message = e.getMessage();
	        		if(attempts > 0 
	        				&& message.contains(HTTP_ERROR) 
	        				&& message.contains(HTTP_502)) {
	        			
	        			LOGGER.info(String.format(
	        					"Failed attempt for (%s) retrying with" +
	        					" (%d) attempts left",fileUrl,attempts));
	        		} else {
	        			throw e;
	        		}
	        	}
	        }

		} catch (IOException e) {
			String msg = fileUrl + " - " + e;
			LOGGER.info("Unable to retrieve:" + msg);
			
			throw new ResourceNotAvailableException(msg, fileUrl, e);
		}
		return r;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
        }

	public String getReplace() {
		return replace;
	}

	public void setReplace(String replace) {
		this.replace = replace;
        }

	public void shutdown() throws IOException {
		// no-op
	}

	/**
	 * @return the number of attempts to fetch resources with an HTTP 502 
	 * failure.
	 */
	public int getRetries() {
		return retries;
	}

	/**
	 * @param retries the number of attempts to fetch resources with an HTTP 502 
	 * failure.
	 */
	public void setRetries(int retries) {
		this.retries = retries;
	}

	
	public String getIncludeFilter() {
		return includeFilter;
	}

	public void setIncludeFilter(String includeFilter) {
		this.includeFilter = includeFilter;
	}
}
