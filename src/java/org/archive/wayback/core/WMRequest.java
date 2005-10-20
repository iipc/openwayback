/* WMRequest
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.core;

import java.text.ParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;

/**
 * Abstraction of all the data associated with a users request to the Wayback
 * Machine.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WMRequest {
	private final Pattern IMAGE_REGEX = Pattern
			.compile(".*\\.(jpg|jpeg|gif|png|bmp|tiff|tif)$");

	private String exactDateRequest = null;

	private Timestamp exactTimestamp = null;

	private Timestamp startTimestamp = null;

	private Timestamp endTimestamp = null;

	private String referrerUrl = null;

	private UURI requestURI = null;

	private UURI redirectURI = null;

	private boolean retrieval = false;

	private boolean query = false;

	private boolean pathQuery = false;

	/**
	 * Constructor
	 */
	public WMRequest() {
		super();
	}

	private void resetType() {
		this.retrieval = false;
		this.query = false;
		this.pathQuery = false;
	}

	/**
	 * sets the request type to PathQuery
	 */
	public void setPathQuery() {
		resetType();
		this.pathQuery = true;
	}

	/**
	 * sets the request type to Query
	 */
	public void setQuery() {
		resetType();
		this.query = true;
	}

	/**
	 * sets the request type to Retrieval/Replay
	 */
	public void setRetrieval() {
		resetType();
		this.retrieval = true;
	}

	/**
	 * @return whether this request appears to be for an image, using only the
	 *         requested URL
	 */
	public boolean isImageRetrieval() {
		String uri = requestURI.getEscapedURI();
		Matcher matcher = null;
		matcher = IMAGE_REGEX.matcher(uri);
		if (matcher != null && matcher.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * @return Returns the endTimestamp.
	 */
	public Timestamp getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * @param endTimestamp
	 *            The endTimestamp to set.
	 */
	public void setEndTimestamp(Timestamp endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * @return Returns the exactDateRequest.
	 */
	public String getExactDateRequest() {
		return exactDateRequest;
	}

	/**
	 * @param exactDateRequest
	 *            The exactDateRequest to set.
	 */
	public void setExactDateRequest(String exactDateRequest) {
		this.exactDateRequest = exactDateRequest;
	}

	/**
	 * @return Returns the exactTimestamp.
	 */
	public Timestamp getExactTimestamp() {
		return exactTimestamp;
	}

	/**
	 * @param exactTimestamp
	 *            The exactTimestamp to set.
	 */
	public void setExactTimestamp(Timestamp exactTimestamp) {
		this.exactTimestamp = exactTimestamp;
	}

	/**
	 * @return Returns the redirectURI.
	 */
	public UURI getRedirectURI() {
		return redirectURI;
	}

	/**
	 * @param redirectURI
	 *            The redirectURI to set.
	 */
	public void setRedirectURI(UURI redirectURI) {
		this.redirectURI = redirectURI;
	}

	/**
	 * @return Returns the referrerUrl.
	 */
	public String getReferrerUrl() {
		return referrerUrl;
	}

	/**
	 * @param referrerUrl
	 *            The referrerUrl to set.
	 */
	public void setReferrerUrl(String referrerUrl) {
		this.referrerUrl = referrerUrl;
	}

	/**
	 * @return Returns the requestURI.
	 */
	public UURI getRequestURI() {
		return requestURI;
	}

	/**
	 * @param requestURI
	 *            The requestURI to set.
	 */
	public void setRequestURI(UURI requestURI) {
		this.requestURI = requestURI;
	}

	/**
	 * @return Returns the startTimestamp.
	 */
	public Timestamp getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * @param startTimestamp
	 *            The startTimestamp to set.
	 */
	public void setStartTimestamp(Timestamp startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	/**
	 * @return Returns the pathQuery.
	 */
	public boolean isPathQuery() {
		return pathQuery;
	}

	/**
	 * @return Returns the query.
	 */
	public boolean isQuery() {
		return query;
	}

	/**
	 * @return Returns the retrieval.
	 */
	public boolean isRetrieval() {
		return retrieval;
	}
	private String getMapParam(Map queryMap,String field) {
		String arr[] = (String[]) queryMap.get(field);
		if(arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}

	/**
	 * @param queryMap
	 * @throws ParseException 
	 * @throws URIException 
	 * @throws IllegalArgumentException 
	 */
	public void parseCGIArgsReplay(Map queryMap) 
		throws ParseException, URIException, IllegalArgumentException {
		
		String requestType = getMapParam(queryMap,"type");
		String requestURIStr = getMapParam(queryMap,"url");
		if(requestType == null) {
			throw new IllegalArgumentException("No type argument");			
		}
		if(requestURIStr == null) {
			throw new IllegalArgumentException("No url argument");
		}

		if(!requestType.equals("replay")) {
			throw new IllegalArgumentException("request type must be 'replay' for this URL");
		}
		parseCGIArgsDates(queryMap);
		if (!requestURIStr.startsWith("http://")) {
			requestURIStr = "http://" + requestURIStr;
		}
		requestURI = new UURI(requestURIStr,false);
		setRetrieval();
	}

	/**
	 * @param queryMap
	 * @throws ParseException 
	 * @throws URIException 
	 * @throws IllegalArgumentException 
	 */
	public void parseCGIArgsQuery(Map queryMap) 
		throws ParseException, URIException, IllegalArgumentException {
		
		String requestType = getMapParam(queryMap,"type");
		String requestURIStr = getMapParam(queryMap,"url");
		if(requestType == null) {
			throw new IllegalArgumentException("No type argument");			
		}
		if(requestURIStr == null) {
			throw new IllegalArgumentException("No url argument");
		}

		if(requestType.equals("query")) {
			setQuery();
		} else if(requestType.equals("pathQuery")) {
			setPathQuery();
		} else {
			throw new IllegalArgumentException("request type must be 'replay' for this URL");
		}
		parseCGIArgsDates(queryMap);
		if (!requestURIStr.startsWith("http://")) {
			requestURIStr = "http://" + requestURIStr;
		}

		requestURI = new UURI(requestURIStr,false);
	}
	
	/**
	 * @param queryMap
	 * @throws ParseException
	 */
	public void parseCGIArgsDates(Map queryMap) throws ParseException {

		// first the exact:
		String origExactDateRequest = getMapParam(queryMap,"date");
		if(origExactDateRequest == null) {
			
			exactTimestamp = Timestamp.currentTimestamp();
			exactDateRequest = exactTimestamp.getDateStr();
			
		} else {
			
			exactTimestamp = Timestamp.parseBefore(origExactDateRequest);
			exactDateRequest = origExactDateRequest;
		}

		// then the starting boundary:
		String startTimestampStr = getMapParam(queryMap,"earliest");
		if(startTimestampStr == null) {
			// no start specified -- if the exact is not specified, assume 
			// the earliest possible:
			if(origExactDateRequest == null) {
				startTimestamp = Timestamp.earliestTimestamp();
			} else {
				// no start specified, but they asked for an exact date.
				// if the exact date was partial, use the earliest possible
				// of the partial:

				if(origExactDateRequest.equals(exactTimestamp.getDateStr())) {
					startTimestamp = Timestamp.earliestTimestamp();			
				} else {
					startTimestamp = Timestamp.parseBefore(exactDateRequest);
				}
			
			}
		} else {
			startTimestamp = Timestamp.parseBefore(startTimestampStr);
		}

		
		// then the ending boundary:
		String endTimestampStr = getMapParam(queryMap,"latest");
		if(endTimestampStr == null) {
			// no end specified -- if the exact is not specified, assume 
			// the latest possible:
			if(origExactDateRequest == null) {
				endTimestamp = Timestamp.latestTimestamp();
			} else {
				// no end specified, but they asked for an exact date.
				// if the exact date was partial, use the latest possible
				// of the partial:

				if(origExactDateRequest.equals(exactTimestamp.getDateStr())) {
					endTimestamp = Timestamp.latestTimestamp();
				} else {
					endTimestamp = Timestamp.parseAfter(exactDateRequest);
				}
			}
		} else {
			endTimestamp = Timestamp.parseAfter(endTimestampStr);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
