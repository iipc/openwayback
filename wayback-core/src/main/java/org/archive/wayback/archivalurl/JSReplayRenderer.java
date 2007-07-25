/* JSRenderer
 *
 * $Id$
 *
 * Created on 1:34:16 PM Nov 8, 2005.
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
package org.archive.wayback.archivalurl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.BaseReplayRenderer;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.util.UrlCanonicalizer;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision:
 *          1483 $
 */
public class JSReplayRenderer extends BaseReplayRenderer {

	private final static String HTTP_LENGTH_HEADER = "Content-Length";

	private final static String HTTP_XFER_ENCODING_HEADER = "Transfer-Encoding";

	private final static String HTTP_LOCATION_HEADER = "Location";

	/**
	 * MIME type of documents which should be marked up with javascript to
	 * rewrite URLs inside document
	 */
	private final static String TEXT_HTML_MIME = "text/html";

	private final static String TEXT_XHTML_MIME = "application/xhtml";

	// TODO: make this configurable
	private final static long MAX_HTML_MARKUP_LENGTH = 1024 * 1024 * 5;

	protected String scriptUrlInserts = null;

	/**
	 * @param list
	 */
	public void setJSInserts(List<String> list) {
		scriptUrlInserts = "";
		for (int i = 0; i < list.size(); i++) {
			scriptUrlInserts += "<script type=\"text/javascript\" src=\""
					+ list.get(i) + "\" ></script>\n";
		}
	}

	/**
	 * test if the SearchResult should be replayed raw, without JS markup
	 * 
	 * @param resource
	 * @param result
	 * @return boolean, true if the document should be returned raw.
	 */
	protected boolean isRawReplayResult(Resource resource, SearchResult result) {

		if (resource.getRecordLength() < MAX_HTML_MARKUP_LENGTH) {
			// TODO: this needs to be configurable such that arbitrary filters
			// can be applied to various mime-types... We'll just hard-code
			// them for now.
			if (-1 != result.get(WaybackConstants.RESULT_MIME_TYPE).indexOf(
					TEXT_HTML_MIME)) {
				return false;
			}
			if (-1 != result.get(WaybackConstants.RESULT_MIME_TYPE).indexOf(
					TEXT_XHTML_MIME)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * omit length and encoding HTTP headers.
	 * 
	 * @param key
	 * @param value
	 * @param uriConverter
	 * @param result
	 * @return String
	 */
	protected String filterHeader(final String key, final String value,
			final ResultURIConverter uriConverter, SearchResult result) {
		String keyUp = key.toUpperCase();
		if (keyUp.equals(HTTP_LENGTH_HEADER.toUpperCase())) {
			return null;
		}
		// TODO: I don't think that this is handled correctly: if the
		// ARC document is chunked, we want to relay that, by NOT omitting the
		// header, but we also need to tell the servlet container not to do
		// any transfer ecoding of it's own "because we probably wanted it to."
		if (keyUp.equals(HTTP_XFER_ENCODING_HEADER.toUpperCase())) {
			return null;
		}
		if (0 == keyUp.indexOf(HTTP_LOCATION_HEADER.toUpperCase())) {
			String baseUrl = result.getAbsoluteUrl();
			String captureDate = result.getCaptureDate();
			String url = UrlCanonicalizer.resolveUrl(baseUrl, value);
			return uriConverter.makeReplayURI(captureDate,url);
		}
		return value;
	}

	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {

		// if we are not returning the exact date they asked for, redirect them:
		if (isExactVersionRequested(wbRequest, result)) {
			super.renderResource(httpRequest, httpResponse, wbRequest, result,
					resource, uriConverter);
		} else {
			String url = result.getAbsoluteUrl();
			String captureDate = result.getCaptureDate();
			String betterURI = uriConverter.makeReplayURI(captureDate,url);
			httpResponse.sendRedirect(betterURI);
		}
	}

//	private void removeString(StringBuilder page, final String toZap) {
//		int idx = page.indexOf(toZap);
//		if (idx >= 0) {
//			page.delete(idx, idx + toZap.length());
//		}
//	}

	/**
	 * add BASE tag and javascript to a page that will rewrite embedded URLs to
	 * point back into the WM. Also attempt to fix up URL attributes in some
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
	protected void markUpPage(StringBuilder page,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, SearchResult result, Resource resource,
			ResultURIConverter uriConverter) {

		String pageUrl = result.getAbsoluteUrl();
		String captureDate = result.getCaptureDate();

		String existingBaseHref = TagMagix.getBaseHref(page);
		if (existingBaseHref != null) {
			pageUrl = existingBaseHref;
		}

		TagMagix.markupTagREURIC(page, uriConverter, captureDate, pageUrl,
				"FRAME", "SRC");
		TagMagix.markupTagREURIC(page, uriConverter, captureDate, pageUrl,
				"META", "URL");
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

	/**
	 * add a BASE HTML tag to make all path relative URLs map to the right URL
	 * 
	 * @param page
	 * @param result
	 */
	protected void insertBaseTag(StringBuilder page, SearchResult result) {
		String resultUrl = result.getAbsoluteUrl();
		String baseTag = "<base href=\"" + resultUrl + "\" />";
		int insertPoint = page.indexOf("<head>");
		if (-1 == insertPoint) {
			insertPoint = page.indexOf("<HEAD>");
		}
		if (-1 == insertPoint) {
			insertPoint = 0;
		} else {
			insertPoint += 6; // just after the tag
		}
		page.insert(insertPoint, baseTag);
	}

	/**
	 * insert Javascript into a page to rewrite URLs
	 * 
	 * @param page
	 * @param httpRequest
	 * @param httpResponse
	 * @param wbRequest
	 * @param result
	 * @param resource
	 * @param uriConverter
	 */
	protected void insertJavascriptXHTML(StringBuilder page,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, SearchResult result, Resource resource,
			ResultURIConverter uriConverter) {

		StringFormatter fmt = wbRequest.getFormatter();

		String resourceTS = result.getCaptureDate();
		Timestamp captureTS = Timestamp.parseBefore(resourceTS);
		Date captureDate = captureTS.getDate();
		String contextPath = uriConverter.makeReplayURI(resourceTS, "");

		StringBuilder ins = new StringBuilder(300);
		ins.append("<script type=\"text/javascript\">\n\n");
		ins.append(fmt.format("ReplayView.javaScriptComment", captureDate,
				new Date()));
		ins.append("var sWayBackCGI = \"" + contextPath + "\";\n");
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
