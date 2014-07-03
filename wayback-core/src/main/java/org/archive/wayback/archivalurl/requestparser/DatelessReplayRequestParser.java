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
package org.archive.wayback.archivalurl.requestparser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.memento.MementoUtils;
import org.archive.wayback.memento.TimeGateBadQueryException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.PathRequestParser;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.AccessPoint;


/**
 * @author brad
 *
 */
public class DatelessReplayRequestParser extends PathRequestParser {

	/**
	 * @param wrapped the BaseRequestParser being wrapped
	 */
	public DatelessReplayRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}
	
	private final static Pattern WB_DATE_PATTERN = Pattern.compile("^(\\d{0,14})$");
	
	

	@Override
	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint accessPoint) throws BadQueryException,
			BetterRequestException {
		
		if (!accessPoint.isEnableMemento()) {
			return super.parse(httpRequest, accessPoint);
		}
		
		String acceptDateTime = httpRequest.getHeader(MementoUtils.ACCEPT_DATETIME);
		
		// Memento TimeGate
		Date date = null;
		
		String requestPath = accessPoint.translateRequestPathQuery(httpRequest);
		
		// FIXME: this flag indicates if Accept-Datetime header had a non-null value,
		// not it's value is valid or not. equivalent of acceptDateTime != null.
		boolean invalidAcceptDateTime = false;
		
		if (acceptDateTime != null) {
			date = MementoUtils.parseAcceptDateTimeHeader(acceptDateTime);
			invalidAcceptDateTime = true;
		}
		
		if (date == null) {
			//TODO: Integrate with Accept-Datetime?
			String acceptTimestamp = httpRequest.getHeader("Accept-Timestamp");
			
			if ((acceptTimestamp != null) && WB_DATE_PATTERN.matcher(acceptTimestamp).matches()) {
				String timestamp = Timestamp.padEndDateStr(acceptTimestamp);
				date = ArchiveUtils.getDate(timestamp, null);
			}
		}
		
		// Accept-Datetime specified but is invalid and timestamp not specified, must return a 400
		if (invalidAcceptDateTime && (date == null)) {
			throw new TimeGateBadQueryException("Invald Memento TimeGate datetime request, Accept-Datetime: " + acceptDateTime, requestPath);
		}
		
		WaybackRequest wbRequest = this.parse(requestPath, accessPoint, date);
		
		if (wbRequest != null) {
			wbRequest.setResultsPerPage(getMaxRecords());
			wbRequest.setMementoTimegate();
		}
		
		return wbRequest;
	}
	
	public WaybackRequest parse(String requestPath, AccessPoint accessPoint)
			throws BetterRequestException, BadQueryException {
		
		return parse(requestPath, accessPoint, null);
	}

	public WaybackRequest parse(String requestPath, AccessPoint accessPoint, Date mementoDate)
			throws BetterRequestException, BadQueryException {
		/*
		 *
		 * We're trying to catch requests without a datespec, in which case,
		 * we just redirect to the same request, inserting today's datespec,
		 * and then we'll let the normal redirection occur.
		 *
		 * The one tricky point is that we don't want to defeat the
		 * server-relative redirection handling, so we want to do some
		 * inspection to make sure it actually looks like an URL, and not like:
		 * 
		 *   images/foo.gif
		 *   redirect.php?blargity=blargblarg
		 * 
		 * What would be perfect is if the user supplied http:// at the front.
		 * 
		 * So, we'll assume that if we see that, we either match, or throw a 
		 * BadQueryException.
		 *   
		 */
		
		String scheme = UrlOperations.urlToScheme(requestPath);
		if (scheme == null) {
			// if it has "http:/" instead of "http://", repair it.
			// (some client canonicalizes "//" in path into "/".)
			if(requestPath.startsWith("http:/")) {
				requestPath = "http://" + requestPath.substring(6);
				scheme = "http://";
			}
		}
		if (scheme == null) {
			try {
				URL u = new URL(UrlOperations.HTTP_SCHEME + requestPath);
				// does the authority look legit?
				if (u.getUserInfo() != null) {
					throw new BadQueryException("Unable to handle URLs with user information");
				}
				
				if (UrlOperations.isAuthority(u.getAuthority())) {
					// ok, we're going to assume this is good:
					return handleDatelessRequest(accessPoint, requestPath, mementoDate);
				}
				
			} catch(MalformedURLException e) {
				// eat it silently
			}
		} else {
			// OK, we're going to assume this is a replay request, sans timestamp,
			// ALWAYS redirect:
			return handleDatelessRequest(accessPoint, requestPath, mementoDate);
		}
		return null;
	}
	
	protected WaybackRequest handleDatelessRequest(AccessPoint accessPoint,
			String requestPath, Date mementoDate) throws BetterRequestException	{
//		String nowTS = Timestamp.currentTimestamp().getDateStr();
//		String newUrl = accessPoint.getUriConverter().makeReplayURI(nowTS, requestPath);
//		throw new BetterRequestException(newUrl);
		
		WaybackRequest wbRequest = new WaybackRequest();
		
		if (wbRequest.getStartTimestamp() == null) {
			wbRequest.setStartTimestamp(getEarliestTimestamp());
		}

		if (wbRequest.getEndTimestamp() == null) {
			wbRequest.setEndTimestamp(getLatestTimestamp());
		}
		
		if (mementoDate == null) {
			mementoDate = new Date();
			wbRequest.setBestLatestReplayRequest();
		}
		
		wbRequest.setReplayDate(mementoDate);
		wbRequest.setAnchorDate(mementoDate);
		wbRequest.setReplayRequest();
		wbRequest.setRequestUrl(requestPath);
		
		return wbRequest;
	}
}
