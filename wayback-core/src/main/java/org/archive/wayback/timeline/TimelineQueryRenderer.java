/* TimelineQueryRenderer
 *
 * $Id$
 *
 * Created on 12:22:28 PM Apr 20, 2006.
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.query.Renderer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TimelineQueryRenderer extends Renderer {

	public void renderUrlResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResults results, ResultURIConverter baseUriConverter)
			throws ServletException, IOException {

		// grab and forward framset adaptor for the uriConverter:
		if (!(baseUriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be " +
					"of class TimelineReplayResultURIConverter");			
		}
		TimelineReplayResultURIConverter uriConverter =
			(TimelineReplayResultURIConverter) baseUriConverter;
		uriConverter.setFramesetMode();

		super.renderUrlResults(httpRequest, httpResponse, wbRequest,
				results, uriConverter);
	}
}
