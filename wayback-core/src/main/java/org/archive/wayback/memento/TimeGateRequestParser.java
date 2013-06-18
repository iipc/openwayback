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
package org.archive.wayback.memento;

import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.WrappedRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 * RequestParser subclass which matches ".../timegate/URL" requests, and parses 
 * the Accept-Datetime header
 * 
 * @consultant Lyudmila Balakireva
 * 
 */
public class TimeGateRequestParser extends WrappedRequestParser implements MementoConstants {
	private static final Logger LOGGER = 
		Logger.getLogger(TimeGateRequestParser.class.getName());

	//private final static String TIMEGATE_SLASH = TIMEGATE + "/";
	//private final static int TIMEGATE_SLASH_LEN = TIMEGATE_SLASH.length();

	/**
	 * @param wrapped
	 *            BaseRequestParser with configuration
	 */
	public TimeGateRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	@Override
	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint accessPoint) throws BadQueryException,
			BetterRequestException {
		
		if (!accessPoint.isEnableMemento()) {
			return null;
		}

		String base = accessPoint.translateRequestPath(httpRequest);
		String requestPath = accessPoint.translateRequestPathQuery(httpRequest);

		LOGGER.fine("requestPath:" + requestPath);
		//if (base.startsWith(TIMEGATE_SLASH)) {

		// strip leading "timegate/":
		//String urlStr = base.substring(TIMEGATE_SLASH_LEN);
		String urlStr = base;
		String acceptDateTime = httpRequest.getHeader(ACCEPT_DATETIME);
		
		// Not a timegate request
		if (acceptDateTime == null) {
			return null;
		}
		
		Date d = MementoUtils.parseAcceptDateTimeHeader(acceptDateTime);
		
		// Accept-Datetime specified but is invalid, must return a 400
		if (d == null) {
			throw new BadQueryException("Invald Memento TimeGate datetime request, Accept-Datetime: " + acceptDateTime);
		}

		WaybackRequest wbRequest = new WaybackRequest();
		
		if (wbRequest.getStartTimestamp() == null) {
			wbRequest.setStartTimestamp(getEarliestTimestamp());
		}

		if (wbRequest.getEndTimestamp() == null) {
			wbRequest.setEndTimestamp(getLatestTimestamp());
		}
		wbRequest.setMementoTimegate(true);
		wbRequest.setReplayDate(d);
		wbRequest.setAnchorDate(d);
		wbRequest.setReplayRequest();
		wbRequest.setRequestUrl(urlStr);
		
		if (wbRequest != null) {
			wbRequest.setResultsPerPage(getMaxRecords());
		}
		
		return wbRequest;
	}
//
//	public WaybackRequest parseOld(HttpServletRequest httpRequest,
//			AccessPoint accessPoint) throws BadQueryException,
//			BetterRequestException {
//
//		String base = accessPoint.translateRequestPath(httpRequest);
//		String requestPath = accessPoint.translateRequestPathQuery(httpRequest);
//
//		LOGGER.fine("requestPath:" + requestPath);
//		if (base.startsWith(TIMEGATE_SLASH)) {
//
//			// strip leading "timegate/":
//			String urlStr = base.substring(TIMEGATE_SLASH_LEN);
//			String acceptDateTime = httpRequest.getHeader(ACCPEPT_DATETIME);
//			Date d = null;
//			if(acceptDateTime != null) {
//				// OK, looks like a valid request -- hopefully urlStr is valid..
//				d = MementoUtils.parseAcceptDateTimeHeader(acceptDateTime);				
//			}
//			if(d == null) {
//				d = new Date();
//			}
//			// get the "Accept-Datetime" header:
////			String httpdate = getHttpDate(httpRequest);
////			Date dtconnegdate = null;
////			if (httpdate != null) {
////				dtconnegdate = checkDateValidity(httpdate, dtsupportedformats);
////				if (dtconnegdate == null) {
////					httpdate="unparsable";
////				}
////			} else {
////				// TODO: should this return null her? no header..
////			}
//
//			WaybackRequest wbRequest = new WaybackRequest();
//			if (wbRequest.getStartTimestamp() == null) {
//				wbRequest.setStartTimestamp(getEarliestTimestamp());
//			}
//			if (dtconnegdate != null) {
//				wbRequest.setReplayDate(dtconnegdate);
////				wbRequest.setAnchorDate(dtconnegdate);
//			} else {
//				wbRequest.setAnchorTimestamp(getLatestTimestamp());
//
//			}
//
//			wbRequest.put("dtconneg", httpdate);
//
//			if (wbRequest.getEndTimestamp() == null) {
//				wbRequest.setEndTimestamp(getLatestTimestamp());
//			}
//			wbRequest.setCaptureQueryRequest();
//			wbRequest.setRequestUrl(urlStr);
//			if (wbRequest != null) {
//				wbRequest.setResultsPerPage(getMaxRecords());
//			}
//			return wbRequest;
//		}
//		return null;
//	}

}
