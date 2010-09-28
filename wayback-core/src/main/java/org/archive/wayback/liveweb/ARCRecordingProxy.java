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
package org.archive.wayback.liveweb;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.util.webapp.AbstractRequestHandler;
import org.archive.wayback.util.webapp.ShutdownListener;

/**
 * @author brad
 *
 */
public class ARCRecordingProxy extends AbstractRequestHandler 
implements ShutdownListener {

	private final static String EXPIRES_HEADER = "Expires";
	private long expiresMS = 60 * 60 * 1000;
	private long fakeExpiresMS = 5 * 60 * 1000;
	private final static String ARC_RECORD_CONTENT_TYPE = 
		"application/x-arc-record";

	private static final Logger LOGGER = 
		Logger.getLogger(ARCRecordingProxy.class.getName());

	private ARCCacheDirectory arcCacheDir = null;
	private URLtoARCCacher cacher = null;

	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		
		StringBuffer sb = httpRequest.getRequestURL();
		String query = httpRequest.getQueryString();
		if(query != null) {
			sb.append("?").append(query);
		}
		FileRegion r = null;
		try {

			String url = sb.toString();
			LOGGER.info("Caching URL(" + url + ")");
			r = cacher.cacheURL(url, arcCacheDir);

			httpResponse.setStatus(HttpServletResponse.SC_OK);
			httpResponse.setContentLength((int)r.getLength());
			httpResponse.setContentType(ARC_RECORD_CONTENT_TYPE);
			long exp = System.currentTimeMillis();
			exp += (r.isFake ? fakeExpiresMS : expiresMS);
			
			httpResponse.setDateHeader(EXPIRES_HEADER, exp);

			r.copyToOutputStream(httpResponse.getOutputStream());

		} catch (URIException e) {
			
			e.printStackTrace();
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		return true;
	}
	
	/**
	 * @return the arcCacheDir
	 */
	public ARCCacheDirectory getArcCacheDir() {
		return arcCacheDir;
	}

	/**
	 * @param arcCacheDir the arcCacheDir to set
	 */
	public void setArcCacheDir(ARCCacheDirectory arcCacheDir) {
		this.arcCacheDir = arcCacheDir;
	}

	/**
	 * @return the cacher
	 */
	public URLtoARCCacher getCacher() {
		return cacher;
	}

	/**
	 * @param cacher the cacher to set
	 */
	public void setCacher(URLtoARCCacher cacher) {
		this.cacher = cacher;
	}

	/**
	 * @return the expiresMS
	 */
	public long getExpiresMS() {
		return expiresMS;
	}

	/**
	 * @param expiresMS the expiresMS to set
	 */
	public void setExpiresMS(long expiresMS) {
		this.expiresMS = expiresMS;
	}

	/**
	 * @return the fakeExpiresMS
	 */
	public long getFakeExpiresMS() {
		return fakeExpiresMS;
	}

	/**
	 * @param fakeExpiresMS the fakeExpiresMS to set
	 */
	public void setFakeExpiresMS(long fakeExpiresMS) {
		this.fakeExpiresMS = fakeExpiresMS;
	}

	public void shutdown() {
		arcCacheDir.shutdown();
		
	}
}
