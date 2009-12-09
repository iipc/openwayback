/* ARCCachingProxy
 *
 * $Id$:
 *
 * Created on Dec 8, 2009.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.archive.io.arc.ARCLocation;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;
import org.archive.wayback.webapp.ServletRequestContext;

/**
 * @author brad
 *
 */
public class ARCCachingProxy extends ServletRequestContext {
	
	private final static String EXPIRES_HEADER = "Expires";

	private final static String ARC_RECORD_CONTENT_TYPE = "application/x-arc-record";
	private static final Logger LOGGER = Logger.getLogger(
			ARCCachingProxy.class.getName());
	private ARCCacheDirectory arcCacheDir = null;
	private URLCacher cacher = null;
	private long expiresMS = 60 * 60 * 1000;
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
		URL url = new URL(sb.toString());
		FileRegion r = null;
		try {
			r = getLiveResource(url);
			httpResponse.setStatus(httpResponse.SC_OK);
			httpResponse.setContentLength((int)r.getLength());
			httpResponse.setContentType(ARC_RECORD_CONTENT_TYPE);
			httpResponse.setDateHeader("Expires", System.currentTimeMillis() + expiresMS);
			r.copyToOutputStream(httpResponse.getOutputStream());
			
		} catch (LiveDocumentNotAvailableException e) {
			
			e.printStackTrace();
			httpResponse.sendError(httpResponse.SC_NOT_FOUND);
		}
//		httpResponse.setContentType("text/plain");
//		PrintWriter pw = httpResponse.getWriter();
//		pw.println("PathInfo:" + httpRequest.getPathInfo());
//		pw.println("RequestURI:" + httpRequest.getRequestURI());
//		pw.println("RequestURL:" + httpRequest.getRequestURL());
//		pw.println("QueryString:" + httpRequest.getQueryString());
//		pw.println("PathTranslated:" + httpRequest.getPathTranslated());
//		pw.println("ServletPath:" + httpRequest.getServletPath());
//		pw.println("ContextPath:" + httpRequest.getContextPath());
//		if(r != null) {
//			pw.println("CachePath:" + r.file.getAbsolutePath());
//			pw.println("CacheStart:" + r.start);
//			pw.println("CacheEnd:" + r.end);
//		} else {
//			pw.println("FAILED CACHE!");
//		}

		return true;
	}
	
	
	private FileRegion getLiveResource(URL url)
	throws LiveDocumentNotAvailableException, IOException {
	
	Resource resource = null;
	
	LOGGER.info("Caching URL(" + url.toString() + ")");
	FileRegion region = cacher.cache2(arcCacheDir, url.toString());
	if(region != null) {
		LOGGER.info("Cached URL(" + url.toString() + ") in " +
				"ARC(" + region.file.getAbsolutePath() + ") at (" 
				+ region.start + " - " + region.end + ")");

	} else {
		throw new IOException("No location!");
	}
	
	return region;
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
	public URLCacher getCacher() {
		return cacher;
	}

	/**
	 * @param cacher the cacher to set
	 */
	public void setCacher(URLCacher cacher) {
		this.cacher = cacher;
	}
}
