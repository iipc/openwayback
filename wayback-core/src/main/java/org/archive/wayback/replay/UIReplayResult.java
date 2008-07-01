/* UIReplayResult
 *
 * $Id$
 *
 * Created on 12:42:09 PM Apr 24, 2006.
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
package org.archive.wayback.replay;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UIReplayResult extends UIResults {
	
	private HttpServletRequest httpRequest;
	private CaptureSearchResult result;
	private CaptureSearchResults results;
	private Resource resource;

	
	/**
	 * Constructor -- chew search result summaries into format easier for JSPs
	 * to digest.
	 *  
	 * @param httpRequest 
	 * @param wbRequest 
	 * @param result
	 * @param results
	 * @param resource
	 * @param uriConverter 
	 * @throws IOException 
	 */
	public UIReplayResult(HttpServletRequest httpRequest, 
			WaybackRequest wbRequest, CaptureSearchResult result,
			CaptureSearchResults results, Resource resource, 
			ResultURIConverter uriConverter) 
	throws IOException {
		
		super(wbRequest,uriConverter);
		this.httpRequest = httpRequest;
		this.result = result;
		this.results = results;
		this.resource = resource;
	}

	/**
	 * @return Returns the httpRequest.
	 */
	public HttpServletRequest getHttpRequest() {
		return httpRequest;
	}

	/**
	 * @return Returns the resource.
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @return Returns the result.
	 */
	public CaptureSearchResult getResult() {
		return result;
	}

	/**
	 * @return the original URL, or at least as close as can be rebuilt from
	 * the index info
	 */
	public String getOriginalUrl() {
		return result.getOriginalUrl();
	}
	/**
	 * @return the MimeURL key from the index of the result
	 */
	public String getUrlKey() {
		return result.getUrlKey();
	}
	/**
	 * @return a string offset+arc file name combo, which should uniquely
	 * identify this document
	 */
	public String getArchiveID() {
		return result.getOffset() + "/" + result.getFile();
	}
	/**
	 * @return the CaptureDate Timestamp of the result
	 */
	public Timestamp getCaptureTimestamp() {
		return Timestamp.parseBefore(result.getCaptureTimestamp());
	}
	/**
	 * @return the MimeType String of the result
	 */
	public String getMimeType() {
		return result.getMimeType();
	}

	/**
	 * @return the Digest string of the result
	 */
	public String getDigest() {
		return result.getDigest();
	}

	/**
	 * @return the HTTP Headers as Properties
	 */
	public Map<String,String> getHttpHeaders() {
		return resource.getHttpHeaders();
	}

	public CaptureSearchResults getResults() {
		return results;
	}

	public void setResults(CaptureSearchResults results) {
		this.results = results;
	}
}
