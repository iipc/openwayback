/* QueryServlet
 *
 * $Id$
 *
 * Created on 2:42:50 PM Nov 7, 2005.
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
package org.archive.wayback.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.core.WaybackServlet;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.WaybackException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class QueryServlet extends WaybackServlet {
	/**
	 * 
	 */
	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final long serialVersionUID = 1L;

	private OpenSearchQueryParser qp = new OpenSearchQueryParser();

	/**
	 * Constructor
	 */
	public QueryServlet() {
		super();
	}

	public void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		WaybackRequest wbRequest = (WaybackRequest) httpRequest
				.getAttribute(WMREQUEST_ATTRIBUTE);

		QueryRenderer renderer;
		try {
			renderer = wayback.getQueryRenderer();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}

		try {

			ResourceIndex idx = wayback.getResourceIndex();
			ResultURIConverter uriConverter = wayback.getURIConverter();

			if (wbRequest == null) {
				wbRequest = qp.parseQuery(httpRequest);
			}

			SearchResults results;

			results = idx.query(wbRequest);

			if (wbRequest.get(WaybackConstants.REQUEST_TYPE).equals(
					WaybackConstants.REQUEST_URL_QUERY)) {
				
                // Annotate the closest matching hit so that it can 
                // be retrieved later from the xml.
                try {
                    annotateClosest(results, wbRequest, httpRequest);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
               
				renderer.renderUrlResults(httpRequest, httpResponse,
						wbRequest, results, uriConverter);

			} else if (wbRequest.get(WaybackConstants.REQUEST_TYPE).equals(
					WaybackConstants.REQUEST_REPLAY_QUERY)) {
				
				// You ask what a REPLAY request is doing in a QueryServlet?
				// In one mode, with a remote BDB JE index, and HTTP-XML as 
				// transport, the whole request is wrapped up as an OpenSearch 
				// query, and the results are marshalled/unmarshalled to/from 
				// XML. In this case, a REPLAY request may be recieved and 
				// handled by this class.
				renderer.renderUrlResults(httpRequest, httpResponse,
						wbRequest, results, uriConverter);

			} else if (wbRequest.get(WaybackConstants.REQUEST_TYPE).equals(
					WaybackConstants.REQUEST_CLOSEST_QUERY)) {
				
				renderer.renderUrlResults(httpRequest, httpResponse,
						wbRequest, results, uriConverter);

			} else if (wbRequest.get(WaybackConstants.REQUEST_TYPE).equals(
					WaybackConstants.REQUEST_URL_PREFIX_QUERY)) {
				
				renderer.renderUrlPrefixResults(httpRequest, httpResponse,
						wbRequest, results, uriConverter);
			} else {
				throw new BadQueryException("Unknown query " +
						wbRequest.get(WaybackConstants.REQUEST_TYPE));
			}

		} catch (WaybackException wbe) {

			renderer.renderException(httpRequest, httpResponse, wbRequest, wbe);

		}
	}
    
    // Method annotating the searchresult closest in time to the timestamp 
    // belonging to this request.
    private void annotateClosest(SearchResults results,
            WaybackRequest wbRequest, HttpServletRequest request) throws ParseException {

        SearchResult closest = null;
        long closestDistance = 0;
        SearchResult cur = null;
        String id = request.getHeader("Proxy-Id");
        if(id == null) id = request.getRemoteAddr();
        String requestsDate = Timestamp.getTimestampForId(request.getContextPath(),id);
        Timestamp wantTimestamp;
        wantTimestamp = Timestamp.parseBefore(requestsDate);

        Iterator itr = results.iterator();
        while (itr.hasNext()) {
            cur = (SearchResult) itr.next();
            long curDistance;
            Timestamp curTimestamp = Timestamp.parseBefore(cur
                    .get(WaybackConstants.RESULT_CAPTURE_DATE));
            curDistance = curTimestamp.absDistanceFromTimestamp(wantTimestamp);
            
            if ((closest == null) || (curDistance < closestDistance)) {
                closest = cur;
                closestDistance = curDistance;
            }
        }
        closest.put("closest", "true");
    }
}
