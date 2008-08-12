/* DomainPrefixPageReplayRenderer
 *
 * $Id$
 *
 * Created on 3:30:30 PM Jul 15, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
package org.archive.wayback.domainprefix;

import java.io.IOException;
import java.util.Iterator;
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
import org.archive.wayback.replay.TextDocument;
import org.archive.wayback.replay.TextReplayRenderer;
import org.archive.wayback.replay.HttpHeaderProcessor;
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

		List<String> jspInserts = getJspInserts();

		StringBuilder toInsert = new StringBuilder(300);

		if(jspInserts != null) {
			Iterator<String> itr = jspInserts.iterator();
			while(itr.hasNext()) {
				toInsert.append(page.includeJspString(itr.next(), httpRequest, 
						httpResponse, wbRequest, results, result, resource));
			}
		}

		page.insertAtEndOfBody(toInsert.toString());

	}
}
