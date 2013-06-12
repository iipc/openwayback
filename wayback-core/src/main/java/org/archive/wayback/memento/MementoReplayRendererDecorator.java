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
import org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadContentException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.replay.HttpHeaderProcessor;
import org.archive.wayback.replay.ReplayRendererDecoratorFactory;
import org.archive.wayback.replay.TextReplayRenderer;
import org.archive.wayback.replay.ReplayRendererDecorator;

/**
 * @author brad
 *
 */
public class MementoReplayRendererDecorator extends ReplayRendererDecorator {

//	public MementoReplayRendererDecorator() {
//		super();
//	}
//	/**
//	 * @param decorated
//	 * @param httpHeaderProcessor
//	 */
//	public MementoReplayRendererDecorator(ReplayRenderer decorated) {
//		super(decorated);
//	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
			throws ServletException, IOException, WaybackException {
		
		// add Memento headers:
		MementoUtils.addMementoHeaders(httpResponse, results, wbRequest);

		decorated.renderResource(httpRequest, httpResponse, wbRequest, result,
				resource, uriConverter, results);
	}
}
