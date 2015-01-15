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
package org.archive.wayback.domainprefix;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.HttpHeaderProcessor;
import org.archive.wayback.replay.TextDocument;
import org.archive.wayback.replay.TextReplayRenderer;
import org.archive.wayback.util.Timestamp;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DomainPrefixTextReplayRenderer extends TextReplayRenderer {
	/**
	 * @param httpHeaderProcessor
	 */
	public DomainPrefixTextReplayRenderer(
			HttpHeaderProcessor httpHeaderProcessor) {
		super(httpHeaderProcessor);
	}

	private final static Pattern httpPattern = 
		Pattern.compile("(http://[^/]*/)");

	protected void updatePage(TextDocument page, 
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
		throws ServletException, IOException {
		String resourceTS = result.getCaptureTimestamp();
		String captureTS = Timestamp.parseBefore(resourceTS).getDateStr();
		
		
		StringBuilder sb = page.sb;
		StringBuffer replaced = new StringBuffer(sb.length());
		Matcher m = httpPattern.matcher(sb);
		while(m.find()) {
			String host = m.group(1);
			String replacement = uriConverter.makeReplayURI(captureTS,host);
			m.appendReplacement(replaced, replacement);
		}
		m.appendTail(replaced);
		// blasted StringBuilder/StringBuffer... gotta convert again...
		page.sb.setLength(0);
		page.sb.ensureCapacity(replaced.length());
		page.sb.append(replaced);

		page.insertAtEndOfBody(buildInsertText(page, httpRequest, httpResponse,
				wbRequest, results, result, resource));
	}
}
