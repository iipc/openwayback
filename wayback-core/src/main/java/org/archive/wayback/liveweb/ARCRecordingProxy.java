/* ARCRecordingProxy
 *
 * $Id$:
 *
 * Created on Apr 1, 2010.
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

package org.archive.wayback.liveweb;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.archive.wayback.webapp.ServletRequestContext;

/**
 * @author brad
 *
 */
public class ARCRecordingProxy extends ServletRequestContext {

	private final static String EXPIRES_HEADER = "Expires";
	private long expiresMS = 60 * 60 * 1000;
	private long fakeExpiresMS = 5 * 60 * 1000;
	private final static String ARC_RECORD_CONTENT_TYPE = 
		"application/x-arc-record";

	private static final Logger LOGGER = 
		Logger.getLogger(ARCRecordingProxy.class.getName());

	private ARCCacheDirectory arcCacheDir = null;
	private URLtoARCCacher cacher = null;
	/* (non-Javadoc)
	 * @see org.archive.wayback.webapp.ServletRequestContext#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
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
}
