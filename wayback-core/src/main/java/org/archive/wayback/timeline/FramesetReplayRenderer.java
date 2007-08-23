/* FramesetReplayRenderer
 *
 * $Id$
 *
 * Created on 4:54:51 PM Apr 17, 2006.
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
package org.archive.wayback.timeline;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.proxy.RawReplayRenderer;
import org.archive.wayback.util.StringFormatter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FramesetReplayRenderer extends RawReplayRenderer {
	
	/**
	 * @param httpRequest
	 * @param httpResponse
	 * @param wbRequest
	 * @param result
	 * @param resource
	 * @param baseUriConverter
	 * @throws ServletException
	 * @throws IOException
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter baseUriConverter) throws ServletException,
			IOException {
		

		if (resource == null) {
			throw new IllegalArgumentException("No resource");
		}
		if (result == null) {
			throw new IllegalArgumentException("No result");
		}
		if (!(baseUriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be " +
					"of class TimelineReplayResultURIConverter");			
		}
		TimelineReplayResultURIConverter uriConverter =
			(TimelineReplayResultURIConverter) baseUriConverter;
		
		if(isExactVersionRequested(wbRequest,result)) {

			// HACKHACK: fake out the wbRequest to get the right arguments:
			wbRequest.put(WaybackConstants.REQUEST_TYPE,
					WaybackConstants.REQUEST_CLOSEST_QUERY);

			// always use the "timeline" adapter for the top frame:
			uriConverter.setTimelineMode();
			String captureDate = result.getCaptureDate();
			String url = result.getAbsoluteUrl();
			String timelineURL = uriConverter.makeReplayURI(captureDate,url);

			// switch between the "inline" and the "meta" adapters based on the
			// REQUEST_META_MODE flag for the bottom frame:
			String isMeta = wbRequest.get(WaybackConstants.REQUEST_META_MODE);
			if(isMeta != null && isMeta.equals("yes")) {
				uriConverter.setMetaMode();
			} else {
				uriConverter.setInlineMode();			
			}
			StringFormatter fmt = wbRequest.getFormatter();
			String replayURL = uriConverter.makeReplayURI(captureDate,url);
			String title = fmt.format("TimelineView.frameSetTitle");

			StringBuilder framesetHTML = new StringBuilder(600);
			framesetHTML.append("<html>");
			framesetHTML.append("<head><title>");
			framesetHTML.append(title);
			framesetHTML.append("</title><head>");

			framesetHTML.append("<FRAMESET frameborder=no border=0 ");
			framesetHTML.append("framespacing=5 marginheight=0 marginwidth=0 ");
			framesetHTML.append("bordercolor=black rows=\"63,*\">");

			framesetHTML.append("<frame scrolling=no src=\"");
			framesetHTML.append(timelineURL);
			framesetHTML.append("\" name=\"wb_timeline\"/>");

			framesetHTML.append("<frame src=\"");
			framesetHTML.append(replayURL);
			framesetHTML.append("\" name=\"wb_replay\" />");

			framesetHTML.append("</frameset>");

			framesetHTML.append("<noframes>");
			framesetHTML.append(fmt.format("TimelineView.frameSetNoFramesMessage"));
			framesetHTML.append("</noframes>");

			framesetHTML.append("</html>");

			httpResponse.addHeader("Content-Type","text/html; charset=utf-8");
			OutputStream os = httpResponse.getOutputStream();
			os.write(framesetHTML.toString().getBytes("UTF-8"));

		} else {

			// not exact version: redirect-o-rama:
			uriConverter.setFramesetMode();
			String captureDate = result.getCaptureDate();
			String url = result.getAbsoluteUrl();
			String betterURI = uriConverter.makeReplayURI(captureDate,url);
			httpResponse.sendRedirect(betterURI);

		}
	}

}
