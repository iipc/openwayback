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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.replay.BaseReplayRenderer;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class JSReplayRenderer extends BaseReplayRenderer {
	
	private final static String REPLAY_JS_URI= "jsuri";

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
	
	protected String javascriptURI = null;

	public void init(Properties p) throws ConfigurationException {
		javascriptURI = (String) p.get( REPLAY_JS_URI);
		if (javascriptURI == null || javascriptURI.length() <= 0) {
			throw new ConfigurationException("Failed to find " + 
					REPLAY_JS_URI);
		}
		super.init(p);
	}
	
	/** test if the SearchResult should be replayed raw, without JS markup
	 * @param resource
	 * @param result
	 * @return boolean, true if the document should be returned raw.
	 */
	protected boolean isRawReplayResult(Resource resource, SearchResult result) {

		if(resource.getRecordLength() < MAX_HTML_MARKUP_LENGTH) {
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
		if(key.equals(HTTP_LENGTH_HEADER)) {
			return null;
		}
		// TODO: I don't think that this is handled correctly: if the
		// ARC document is chunked, we want to relay that, by NOT omitting the
		// header, but we also need to tell the servlet container not to do
		// any transfer ecoding of it's own "because we probably wanted it to."
		if(key.equals(HTTP_XFER_ENCODING_HEADER)) {
			return null;
		}
		if (0 == key.indexOf(HTTP_LOCATION_HEADER)) {
			return uriConverter.makeRedirectReplayURI(result, value);
		}
		return value;
	}

	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {

		// if we are not returning the exact date they asked for, redirect them:
		if(isExactVersionRequested(wbRequest,result)) {
			super.renderResource(httpRequest, httpResponse, wbRequest,
					result, resource, uriConverter);
		} else {
			String betterURI = uriConverter.makeReplayURI(result);
			httpResponse.sendRedirect(betterURI);			
		}
	}

	/** 
	 * add BASE tag and javascript to a page that will rewrite embedded URLs 
	 * to point back into the WM. Also attempt to fix up URL attributes in some
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

		String pageUrl = result.get(WaybackConstants.RESULT_URL);

		String existingBaseHref = TagMagix.getBaseHref(page);
		if(existingBaseHref != null) {
			pageUrl = existingBaseHref;
		}
		
		TagMagix.markupTagREURIC(page, uriConverter,result,pageUrl, "FRAME", "SRC");
		TagMagix.markupTagREURIC(page, uriConverter,result,pageUrl, "META", "URL");
		TagMagix.markupTagREURIC(page, uriConverter,result,pageUrl, "LINK", "HREF");
		// TODO: The classic WM added a js_ to the datespec, so NotInArchives
		// can return an valid javascript doc, and not cause Javascript errors.
		TagMagix.markupTagREURIC(page, uriConverter,result,pageUrl, "SCRIPT", "SRC");

		if(existingBaseHref == null) {
			insertBaseTag(page, result);
		}
		insertJavascriptXHTML(page, httpRequest, httpResponse, wbRequest, 
				result, resource, uriConverter);
	}

	/** add a BASE HTML tag to make all path relative URLs map to the right URL
	 * 
	 * @param page
	 * @param result
	 */
	protected void insertBaseTag(StringBuilder page, SearchResult result) {
		String resultUrl = result.get(WaybackConstants.RESULT_URL);
		String baseTag = "<base href=\"http://" + resultUrl + "\" />";
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
		String nowTS = Timestamp.currentTimestamp().getDateStr();

		String contextPath = uriConverter.getReplayUriPrefix(result);

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
				+ "var sWayBackCGI = \""
				+ contextPath
				+ "\";\n"
				+ "</script>\n"
				+ "<script type=\"text/javascript\" src=\""
				+ javascriptURI
				+ "\" ></script>\n";

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
