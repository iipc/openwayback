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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.replay.ReplayRendererDecorator;

/**
 * @author brad
 *
 */
public class MementoReplayRendererDecorator extends ReplayRendererDecorator {

	public MementoReplayRendererDecorator() {
		super();
	}
	/**
	 * @param decorated
	 * @param httpHeaderProcessor
	 */
	public MementoReplayRendererDecorator(ReplayRenderer decorated) {
		super(decorated);
	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
					throws ServletException, IOException, WaybackException {
		renderResource(httpRequest, httpResponse, wbRequest, result, resource,
				resource, uriConverter, results);
	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource, ResultURIConverter uriConverter,
			CaptureSearchResults results) throws ServletException, IOException,
			WaybackException {

		// add Memento headers:
//		UIResults results = UIResults.extractCaptureQuery(request);
//		WaybackRequest wbRequest = results.getWbRequest();
//		CaptureSearchResults cResults = results.getCaptureResults();
//		CaptureSearchResult res = cResults.getClosest();
		String u = wbRequest.getRequestUrl();
		SimpleDateFormat httpformatterl = new SimpleDateFormat(
				"E, dd MMM yyyy HH:mm:ss z");
		TimeZone tzo = TimeZone.getTimeZone("GMT");
		httpformatterl.setTimeZone(tzo);
		SimpleDateFormat formatterk = new SimpleDateFormat("yyyyMMddHHmmss");
		formatterk.setTimeZone(tzo);
		Properties apProps = wbRequest.getAccessPoint().getConfigs();
		Date closestDate = result.getCaptureDate();
		String uriPrefix = wbRequest.getAccessPoint().getReplayPrefix();
		String agguri = apProps.getProperty("aggregationPrefix")
				+ "timebundle/" + u;
		String timemap = " , <"
				+ apProps.getProperty("aggregationPrefix")
				+ "timemap/link/" + u
				+ ">;rel=\"timemap\"; type=\"application/link-format\"";

		String timegate = ",<" + uriPrefix + "timegate/" + u
				+ ">;rel=\"timegate\"";

		Date f = results.getFirstResultDate();
		Date l = results.getLastResultDate();

		StringBuffer sb = new StringBuffer();

		httpResponse.setHeader("Memento-Datetime",
				httpformatterl.format(result.getCaptureDate()));

		String memento = ",<" + uriPrefix + formatterk.format(closestDate)
				+ "/" + u + ">;rel=\"memento\"; datetime=\""
				+ httpformatterl.format(closestDate) + "\"";
		String mfl = null;
		if ((closestDate.equals(f)) && closestDate.equals(l)) {
			mfl = ", <"
					+ uriPrefix
					+ formatterk.format(f)
					+ "/"
					+ u
					+ ">;rel=\"first last memento\"; datetime=\""
					+ httpformatterl.format(f) + "\"";
		} else if (closestDate.equals(f)) {
			mfl = ", <" + uriPrefix + formatterk.format(f) + "/" + u
					+ ">;rel=\"first memento\"; datetime=\""
					+ httpformatterl.format(f) + "\"";
			mfl = mfl + ", <" + uriPrefix + formatterk.format(l) + "/" + u
					+ ">;rel=\"last memento\"; datetime=\""
					+ httpformatterl.format(l) + "\"";

		} else if (closestDate.equals(l)) {
			mfl = ", <" + uriPrefix + formatterk.format(l) + "/" + u
					+ ">;rel=\"last memento\"; datetime=\""
					+ httpformatterl.format(l) + "\"";
			mfl = mfl + ", <" + uriPrefix + formatterk.format(f) + "/" + u
					+ ">;rel=\"first memento\"; datetime=\""
					+ httpformatterl.format(f) + "\"";
		} else {
			mfl = memento;

			mfl = mfl + ", <" + uriPrefix + formatterk.format(l) + "/" + u
					+ ">;rel=\"last memento\"; datetime=\""
					+ httpformatterl.format(l) + "\"";
			mfl = mfl + ", <" + uriPrefix + formatterk.format(f) + "/" + u
					+ ">;rel=\"first memento\"; datetime=\""
					+ httpformatterl.format(f) + "\"";
		}

		sb = new StringBuffer(mfl);

		// calculate closest values for  link header

		CaptureSearchResult closestleft = null;
		CaptureSearchResult closestright = null;
		long rclosestDistance = 0;
		long lclosestDistance = 0;
		CaptureSearchResult cur = null;

		long wantTime = closestDate.getTime();

		Iterator<CaptureSearchResult> itr = results.iterator();
		while (itr.hasNext()) {
			cur = itr.next();
			cur.getCaptureDate();
			long curDistance = cur.getCaptureDate().getTime() - wantTime;
			// == 0 skip
			if (curDistance > 0) {
				if ((closestright == null)
						|| (Math.abs(curDistance) < Math
								.abs(rclosestDistance))) {
					closestright = cur;
					rclosestDistance = Math.abs(curDistance);
				}
			}

			if (curDistance < 0) {
				if ((closestleft == null)
						|| (Math.abs(curDistance) < Math
								.abs(lclosestDistance))) {
					closestleft = cur;
					lclosestDistance = Math.abs(curDistance);
				}
			}

		}

		if (closestleft != null) {
			if (!(closestleft.getCaptureDate().equals(f))) {

				sb.append(", <"
						+ uriPrefix
						+ formatterk.format(closestleft.getCaptureDate())
						+ "/"
						+ u
						+ ">;rel=\"prev memento\"; datetime=\""
						+ httpformatterl.format(closestleft
								.getCaptureDate()) + "\"");
			} else {
				int m_index = sb.lastIndexOf("\"first memento\"");
				sb.insert(m_index + 1, "prev ");

			}
		}
		if (closestright != null) {
			if (!(closestright.getCaptureDate().equals(l))) {
				sb.append(", <"
						+ uriPrefix
						+ formatterk.format(closestright.getCaptureDate())
						+ "/"
						+ u
						+ ">;rel=\"next memento\"; datetime=\""
						+ httpformatterl.format(closestright
								.getCaptureDate()) + "\"");
			} else {
				int m_index = sb.lastIndexOf("\"last memento\"");
				sb.insert(m_index + 1, "next ");

			}

		}

		String origlink = ", <" + u + ">;rel=\"original\"";

		httpResponse.setHeader("Link", "<" + agguri + ">;rel=\"timebundle\""
				+ origlink + sb.toString() + timemap + timegate);
		
		decorated.renderResource(httpRequest, httpResponse, wbRequest, result,
				httpHeadersResource, payloadResource, uriConverter, results);
	}
}
