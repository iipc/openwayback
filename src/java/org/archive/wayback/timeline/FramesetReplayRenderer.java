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

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FramesetReplayRenderer extends RawReplayRenderer {
	
	public void renderRedirect(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {
		
		if (!(uriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be " +
					"of class TimelineReplayResultURIConverter");			
		}
		TimelineReplayResultURIConverter turiConverter = 
			(TimelineReplayResultURIConverter) uriConverter;
		ResultURIConverter furiConverter = turiConverter.getFramesetAdapter(
				wbRequest);

		String betterURI = furiConverter.makeReplayURI(result);
		httpResponse.sendRedirect(betterURI);
	}

	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {
		

		if (resource == null) {
			throw new IllegalArgumentException("No resource");
		}
		if (result == null) {
			throw new IllegalArgumentException("No result");
		}
		if (!(uriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be " +
					"of class TimelineReplayResultURIConverter");			
		}
		TimelineReplayResultURIConverter baseUriConverter =
			(TimelineReplayResultURIConverter) uriConverter;
		
		// always use the "timeline" adapter for the top frame:
		ResultURIConverter turiConverter = baseUriConverter.getTimelineAdapter(
				wbRequest);
		
		// switch between the "inline" and the "meta" adapters based on the
		// REQUEST_META_MODE flag:

		ResultURIConverter ruriConverter = null;
		String isMeta = wbRequest.get(WaybackConstants.REQUEST_META_MODE);
		if(isMeta != null && isMeta.equals("yes")) {
			ruriConverter =  baseUriConverter.getMetaAdapter(wbRequest);
		} else {
			ruriConverter =  baseUriConverter.getInlineAdapter(wbRequest);			
		}

		// HACKHACK: fake out the wbRequest to get the right arguments:
		wbRequest.put(WaybackConstants.REQUEST_TYPE,
				WaybackConstants.REQUEST_CLOSEST_QUERY);

		String replayURL = ruriConverter.makeReplayURI(result);
		String timelineURL = turiConverter.makeReplayURI(result);
		String title = "WB-Timeline";

		StringBuilder framesetHTML = new StringBuilder(600);
		framesetHTML.append("<html>");
		framesetHTML.append("<head><title>");
		framesetHTML.append(title);
		framesetHTML.append("</title><head>");

		framesetHTML.append("<FRAMESET frameborder=no border=0 ");
		framesetHTML.append("framespacing=5 marginheight=0 marginwidth=0 ");
		framesetHTML.append("bordercolor=black rows=\"100,*\">");

		framesetHTML.append("<frame scrolling=no src=\"");
		framesetHTML.append(timelineURL);
		framesetHTML.append("\" name=\"wb_timeline\"/>");
		
		framesetHTML.append("<frame src=\"");
		framesetHTML.append(replayURL);
		framesetHTML.append("\" name=\"wb_replay\" />");
		
		framesetHTML.append("</frameset>");

		framesetHTML.append("<noframes>");
		framesetHTML.append("You need a browser that supports frames to ");
		framesetHTML.append("see this, well not exactly _this_, but what ");
		framesetHTML.append("would have been here if you had a ");
		framesetHTML.append("frames-capable browser.");
		framesetHTML.append("</noframes>");

		framesetHTML.append("</html>");

		httpResponse.addHeader("Content-Type","text/html; charset=utf-8");
		OutputStream os = httpResponse.getOutputStream();
		os.write(framesetHTML.toString().getBytes("UTF-8"));
	}

}
