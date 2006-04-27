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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.archivalurl.JSReplayRenderer;
import org.archive.wayback.archivalurl.TagMagix;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TimelineReplayRenderer extends JSReplayRenderer {

	public void renderRedirect(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {
		
		if (!(uriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be " +
					"of class TimelineReplayResultURIConverter");			
		}
		TimelineReplayResultURIConverter baseUriConverter =
			(TimelineReplayResultURIConverter) uriConverter;
		ResultURIConverter ruriConverter = baseUriConverter.getInlineAdapter(
				wbRequest);

		String betterURI = ruriConverter.makeReplayURI(result);
		httpResponse.sendRedirect(betterURI);
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
	 * @param uriConverter
	 */
	protected void markUpPage(StringBuilder page, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) {

		if (!(uriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be " +
					"of class TimelineReplayResultURIConverter");			
		}
		TimelineReplayResultURIConverter baseUriConverter =
			(TimelineReplayResultURIConverter) uriConverter;
		ResultURIConverter furiConverter = baseUriConverter.getFramesetAdapter(
				wbRequest);
		ResultURIConverter ruriConverter = baseUriConverter.getInlineAdapter(
				wbRequest);

		String pageUrl = result.get(WaybackConstants.RESULT_URL);

		String existingBaseHref = TagMagix.getBaseHref(page);
		if(existingBaseHref != null) {
			pageUrl = existingBaseHref;
		}

		TagMagix.markupTagREURIC(page, ruriConverter,result,pageUrl, "FRAME", "SRC");
		TagMagix.markupTagREURIC(page, furiConverter,result,pageUrl, "META", "URL");
		TagMagix.markupTagREURIC(page, ruriConverter,result,pageUrl, "LINK", "HREF");
		// TODO: The classic WM added a js_ to the datespec, so NotInArchives
		// can return an valid javascript doc, and not cause Javascript errors.
		TagMagix.markupTagREURIC(page, ruriConverter,result,pageUrl, "SCRIPT", "SRC");

//			TagMagix.markupTagRE(page, wmPrefix, pageUrl, pageTS, "FRAME", "SRC");
//			TagMagix.markupTagRE(page, wmPrefix, pageUrl, pageTS, "META", "URL");
//			TagMagix.markupTagRE(page, wmPrefix, pageUrl, pageTS, "LINK", "HREF");
//			// TODO: The classic WM added a js_ to the datespec, so NotInArchives
//			// can return an valid javascript doc, and not cause Javascript errors.
//			TagMagix.markupTagRE(page, wmPrefix, pageUrl, pageTS, "SCRIPT", "SRC");
		if(existingBaseHref == null) {
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
	 * @param uriConverter
	 */
	protected void insertJavascriptXHTML(StringBuilder page, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) {

		String resourceTS = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		String resourceUrl = result.get(WaybackConstants.RESULT_URL);
		Timestamp captureTS = Timestamp.parseBefore(resourceTS);
		String nowTS = Timestamp.currentTimestamp().getDateStr();

		if (!(uriConverter instanceof TimelineReplayResultURIConverter)) {
			throw new IllegalArgumentException("ResultURIConverter must be " +
					"of class TimelineReplayResultURIConverter");			
		}
		TimelineReplayResultURIConverter baseUriConverter = 
			(TimelineReplayResultURIConverter) uriConverter;
		
		ResultURIConverter ruriConverter = baseUriConverter.getInlineAdapter(wbRequest);
		ResultURIConverter furiConverter = baseUriConverter.getFramesetAdapter(wbRequest);

		String sWaybackReplayCGI = ruriConverter.getReplayUriPrefix(result);
		
		String sWaybackFramesetCGI = furiConverter.getReplayUriPrefix(result);

		String swmNotice = "Wayback - External links, forms, and search boxes " +
			"may not function within this collection. Url: " + resourceUrl + 
			" time: " + captureTS.prettyDateTime();
		String swmHideNotice = "hide";

		String scriptInsert = "<script type=\"text/javascript\">\n"
				+ "\n"
				+ "//            FILE ARCHIVED ON "
				+ resourceTS
				+ " AND RETRIEVED FROM THE\n"
				+ "//            INTERNET ARCHIVE ON "
				+ nowTS
				+ ".\n"
				+ "//            JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.\n"
				+ "//\n"
				+ "// ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.\n"
				+ "// SECTION 108(a)(3)).\n"
				+ "\n"
				+ "var sWayBackFramesetCGI = \""
				+ sWaybackFramesetCGI
				+ "\";\n"
				+ "var sWayBackReplayCGI = \""
				+ sWaybackReplayCGI
				+ "\";\n"
				+ "var wmNotice = \""
				+ swmNotice
				+ "\";\n"
				+ "var wmHideNotice = \""
				+ swmHideNotice
				+ "\";\n"
				+ "</script>\n"
				+ "<script type=\"text/javascript\" src=\""
				+ javascriptURI
				+ "\" />\n";

		int insertPoint = page.indexOf("</body>");
		if (-1 == insertPoint) {
			insertPoint = page.indexOf("</BODY>");
		}
		if (-1 == insertPoint) {
			insertPoint = page.length();
		}
		page.insert(insertPoint, scriptInsert);
	}
}
