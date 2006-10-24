/* QueryFilter
 *
 * $Id$
 *
 * Created on 1:22:14 PM Nov 8, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
package org.archive.wayback.archivalurl;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.archivalurl.parser.PathDatePrefixParser;
import org.archive.wayback.archivalurl.parser.PathDateRangeParser;
import org.archive.wayback.archivalurl.parser.PathPrefixDatePrefixParser;
import org.archive.wayback.archivalurl.parser.PathPrefixDateRangeParser;
import org.archive.wayback.core.RequestFilter;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ConfigurationException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class QueryFilter extends RequestFilter {

	private int defaultResultsPerPage = 10;
	
	private ArchivalUrlParser parsers[] = { 
			new PathDatePrefixParser(),
			new PathDateRangeParser(),
			new PathPrefixDatePrefixParser(),
			new PathPrefixDateRangeParser()
		};
	
	public void init(Properties p) throws ConfigurationException {
		super.init(p);
		String resultsPerPage = (String) p.get(
				WaybackConstants.RESULTS_PER_PAGE_CONFIG_NAME);
		if(resultsPerPage != null) {
			defaultResultsPerPage = Integer.parseInt(resultsPerPage);
		}
	}

	public WaybackRequest parseRequest(HttpServletRequest httpRequest) {
		String queryString = httpRequest.getQueryString();
		String origRequestPath = httpRequest.getRequestURI();
		if (queryString != null) {
			origRequestPath = httpRequest.getRequestURI() + "?" + queryString;
		}
		String contextPath = httpRequest.getContextPath();
		if (!origRequestPath.startsWith(contextPath)) {
			return null;
		}
		String requestPath = origRequestPath.substring(contextPath.length());
		
		WaybackRequest wbRequest = null; 
		for(int i=0; i< parsers.length; i++) {
			wbRequest = parsers[i].parse(requestPath);
			if(wbRequest != null) break;
		}
		
		if(wbRequest != null) {
			wbRequest.setResultsPerPage(defaultResultsPerPage);
			wbRequest.fixup(httpRequest);
		}
		return wbRequest;
	}
}
