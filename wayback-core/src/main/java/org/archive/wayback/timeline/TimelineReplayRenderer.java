/* TimelineReplayRenderer
 *
 * $Id$
 *
 * Created on 11:43:05 AM Apr 18, 2006.
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
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.archivalurl.JSReplayRenderer;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.TagMagix;
import org.archive.wayback.util.StringFormatter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TimelineReplayRenderer extends JSReplayRenderer {

	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter baseUriConverter) throws ServletException,
			IOException {

		// if we are not returning the exact date they asked for, redirect them:
		if (isExactVersionRequested(wbRequest, result)) {
			super.renderResource(httpRequest, httpResponse, wbRequest, result,
					resource, baseUriConverter);
		} else {
			if (!(baseUriConverter instanceof TimelineReplayResultURIConverter)) {
				throw new IllegalArgumentException("ResultURIConverter must "
						+ "be of class TimelineReplayResultURIConverter");
			}
			TimelineReplayResultURIConverter uriConverter = (TimelineReplayResultURIConverter) baseUriConverter;

			// BUGBUG?? this should be setFramesetMode, right??
			uriConverter.setInlineMode();
			String captureDate = result.getCaptureDate();
			String url = result.getAbsoluteUrl();
			String betterURI = uriConverter.makeReplayURI(captureDate,url);
			httpResponse.sendRedirect(betterURI);
		}
	}

	/** 
	 * add BASE tag and javascript to a page that will rewrite embedded URLs 
	 * to point back into the WM, also attempt to fix up URL attributes in some
	 * tags that must be correct at page load (FRAME, META, LINK, SCRIPT)
	 * 
	 * @param page
	 * @param httpRequest 
	 * @param httpResponse 
	 * @param wbRequest 
	 * @param result
	 * @param resource 
	 * @param baseUriConverter
	 */
	protected void markUpPage(StringBuilder page,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, SearchResult result, Resource resource,
			ResultURIConverter baseUriConverter) {

		if (!(baseUriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be "
					+ "of class TimelineReplayResultURIConverter");
		}
		TimelineReplayResultURIConverter uriConverter = (TimelineReplayResultURIConverter) baseUriConverter;

		String pageUrl = result.getAbsoluteUrl();
		String captureDate = result.getCaptureDate();

		String existingBaseHref = TagMagix.getBaseHref(page);
		if (existingBaseHref != null) {
			pageUrl = existingBaseHref;
		}

		uriConverter.setFramesetMode();
		TagMagix.markupTagREURIC(page, uriConverter, captureDate, pageUrl,
				"META", "URL");

		uriConverter.setInlineMode();
		TagMagix.markupTagREURIC(page, uriConverter, captureDate, pageUrl,
				"FRAME", "SRC");
		TagMagix.markupTagREURIC(page, uriConverter, captureDate, pageUrl,
				"LINK", "HREF");
		// TODO: The classic WM added a js_ to the datespec, so NotInArchives
		// can return an valid javascript doc, and not cause Javascript errors.
		TagMagix.markupTagREURIC(page, uriConverter, captureDate, pageUrl,
				"SCRIPT", "SRC");

		if (existingBaseHref == null) {
			insertBaseTag(page, result);
		}
		insertJavascriptXHTML(page, httpRequest, httpResponse, wbRequest,
				result, resource, uriConverter);
	}

	/** insert Javascript into a page to rewrite URLs
	 * @param page
	 * @param httpRequest 
	 * @param httpResponse 
	 * @param wbRequest 
	 * @param result
	 * @param resource 
	 * @param baseUriConverter
	 */
	protected void insertJavascriptXHTML(StringBuilder page,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, SearchResult result, Resource resource,
			ResultURIConverter baseUriConverter) {

		String resourceTS = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		String resourceUrl = result.get(WaybackConstants.RESULT_URL);
		Timestamp captureTS = Timestamp.parseBefore(resourceTS);
		Date captureDate = captureTS.getDate();

		if (!(baseUriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be "
					+ "of class TimelineReplayResultURIConverter");
		}
		TimelineReplayResultURIConverter uriConverter = (TimelineReplayResultURIConverter) baseUriConverter;

		uriConverter.setInlineMode();
		String sWaybackReplayCGI = uriConverter.makeReplayURI(resourceTS,"");

		uriConverter.setFramesetMode();
		String sWaybackFramesetCGI = uriConverter.makeReplayURI(resourceTS,"");
		StringFormatter fmt = wbRequest.getFormatter();

		String wmNotice = fmt.format("ReplayView.banner", resourceUrl,
				captureDate);
		String wmHideNotice = fmt.format("ReplayView.bannerHideLink");

		StringBuilder ins = new StringBuilder(300);
		ins.append("<script type=\"text/javascript\">\n\n");
		ins.append(fmt.format("ReplayView.javaScriptComment", captureDate,
				new Date()));
		ins.append("var sWayBackFramesetCGI = \"" + sWaybackFramesetCGI
				+ "\";\n");
		ins.append("var sWayBackReplayCGI = \"" + sWaybackReplayCGI + "\";\n");
		ins.append("var wmNotice = \"" + wmNotice + "\";\n");
		ins.append("var wmHideNotice = \"" + wmHideNotice + "\";\n");
		ins.append("</script>\n");
		ins.append(scriptUrlInserts);

		int insertPoint = page.lastIndexOf("</body>");
		if (-1 == insertPoint) {
			insertPoint = page.lastIndexOf("</BODY>");
		}
		if (-1 == insertPoint) {
			insertPoint = page.length();
		}
		page.insert(insertPoint, ins.toString());
	}
}
