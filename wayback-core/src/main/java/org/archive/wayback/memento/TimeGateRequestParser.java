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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
public class TimeGateRequestParser extends WrappedRequestParser {
	private static final Logger LOGGER = 
		Logger.getLogger(TimeGateRequestParser.class.getName());

	String DTHEADER = "Accept-Datetime";

	List<SimpleDateFormat> dtsupportedformats = 
		new ArrayList<SimpleDateFormat>();

	String MEMENTO_BASE = "timegate/";

	/**
	 * @param wrapped
	 *            BaseRequestParser with configuration
	 */
	public TimeGateRequestParser(BaseRequestParser wrapped) {
		super(wrapped);

		dtsupportedformats
				.add(new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z"));
		dtsupportedformats.add(new SimpleDateFormat("E, dd MMM yyyy Z"));
		dtsupportedformats.add(new SimpleDateFormat("E, dd MMM yyyy"));
	}

	@Override
	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint accessPoint) throws BadQueryException,
			BetterRequestException {

		String base = accessPoint.translateRequestPath(httpRequest);
		String requestPath = accessPoint.translateRequestPathQuery(httpRequest);

		LOGGER.fine("requestPath:" + requestPath);
		if (base.startsWith(MEMENTO_BASE)) {

			// strip leading "timegate/":
			String urlStr = base.substring(MEMENTO_BASE.length());

			// get the "Accept-Datetime" header:
			String httpdate = getHttpDate(httpRequest);
			Date dtconnegdate = null;
			if (httpdate != null) {
				dtconnegdate = checkDateValidity(httpdate, dtsupportedformats);
				if (dtconnegdate == null) {
					httpdate="unparsable";
				}
			} else {
				// TODO: should this return null her? no header..
			}

			WaybackRequest wbRequest = new WaybackRequest();
			if (wbRequest.getStartTimestamp() == null) {
				wbRequest.setStartTimestamp(getEarliestTimestamp());
			}
			if (dtconnegdate != null) {
				wbRequest.setReplayDate(dtconnegdate);
//				wbRequest.setAnchorDate(dtconnegdate);
			} else {
				wbRequest.setAnchorTimestamp(getLatestTimestamp());

			}

			wbRequest.put("dtconneg", httpdate);

			if (wbRequest.getEndTimestamp() == null) {
				wbRequest.setEndTimestamp(getLatestTimestamp());
			}
			wbRequest.setCaptureQueryRequest();
			wbRequest.setRequestUrl(urlStr);
			if (wbRequest != null) {
				wbRequest.setResultsPerPage(getMaxRecords());
			}
			return wbRequest;
		}
		return null;
	}

	/**
	 * Extract the value of the "Accept-Datetime" HTTP request header, if 
	 * present, and further strips the date value from any surrounding "{","}"
	 * @param req HttpServletRequest for this request
	 * @return the raw String containing the date information, or null if no
	 * such HTTP header exists.
	 */
	public String getHttpDate(HttpServletRequest req) {
		String httpdate = req.getHeader(DTHEADER);

		if (httpdate != null) {
			int j = httpdate.indexOf("{", 0);

			if (j >= 0) {

				httpdate = httpdate.substring(httpdate.indexOf("{", 0) + 1);

			}

			if (httpdate.indexOf("}") > 0) {
				httpdate = httpdate.substring(0, httpdate.indexOf("}"));

			}
		}
		return httpdate;
	}

	/**
	 * Attempt to parse the String httpdate argument using one of the
	 * SimpleDateFormats provided.
	 * 
	 * @param httpdate
	 *            String version of a Date
	 * @param list
	 *            of SimpleDateFormats to parse the httpdate
	 * @return Date object set to the time parsed, or null if not parsed
	 */
	public Date checkDateValidity(String httpdate, List<SimpleDateFormat> list) {

		Date d = null;
		Iterator<SimpleDateFormat> it = list.iterator();
		while (it.hasNext()) {
			SimpleDateFormat formatter = it.next();
			try {

				d = formatter.parse(httpdate);
				break;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return d;
	}
}
