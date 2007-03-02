/* BaseReplayRenderer
 *
 * $Id$
 *
 * Created on 12:35:07 PM Apr 24, 2006.
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
package org.archive.wayback.replay;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.WaybackException;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.universalchardet.UniversalDetector;


/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class BaseReplayRenderer implements ReplayRenderer {

	// in several places, this class defers generation of client responses
	// to a .jsp file, once the business logic of replaying is done.

	// this constant indicates the name of the configuration in the web.xml
	// where the directory holding all the .jsps is found:
	private final static String JSP_PATH = "replayui.jsppath";

	// and this variable stores the .jsp directory configuration found 
	// at init() time
	protected String jspPath;

	// if documents are marked up before sending to clients, the data is
	// decoded into a String in chunks. This is how big a chunk to decode with.
	private final static int C_BUFFER_SIZE = 4096;

	// hand off this many bytes to the chardet library
	private final static int MAX_CHARSET_READAHEAD = 65536;

	// ...and if the chardet library fails, use the Content-Type header
	private final static String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

	// ...if it also includes "charset="
	private final static String CHARSET_TOKEN = "charset=";

	private final static int BYTE_BUFFER_SIZE = 4096;

	

	protected final Pattern IMAGE_REGEX = Pattern
			.compile(".*\\.(jpg|jpeg|gif|png|bmp|tiff|tif)$");

	private final String ERROR_JSP = "ErrorResult.jsp";

	private final String ERROR_JAVASCRIPT = "ErrorJavascript.jsp";

	private final String ERROR_CSS = "ErrorCSS.jsp";

	// Showing the 1 pixel gif actually blocks the alt text.. better to return
	// a normal error page
//	private final String ERROR_IMAGE = "error_image.gif";
	private final String ERROR_IMAGE = "ErrorResult.jsp";

	/*  INITIALIZATION:  */

	public void init(Properties p) throws ConfigurationException {
		this.jspPath = (String) p.get(JSP_PATH);
		if (this.jspPath == null || this.jspPath.length() <= 0) {
			throw new ConfigurationException("Failed to find " + JSP_PATH);
		}
	}

	/*  ERROR HANDLING RESPONSES:  */

	private boolean requestIsEmbedded(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {
		// without a wbRequest, assume it is not embedded: send back HTML
		if(wbRequest == null) {
			return false;
		}
		String referer = wbRequest.get(WaybackConstants.REQUEST_REFERER_URL);
		return (referer != null && referer.length() > 0);
	}

	private boolean requestIsImage(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {
		String requestUrl = wbRequest.get(WaybackConstants.REQUEST_URL);
		if (requestUrl == null)
			return false;
		Matcher matcher = IMAGE_REGEX.matcher(requestUrl);
		return (matcher != null && matcher.matches());
	}

	private boolean requestIsJavascript(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {

		String requestUrl = wbRequest.get(WaybackConstants.REQUEST_URL);
		return (requestUrl != null) && requestUrl.endsWith(".js");
	}

	private boolean requestIsCSS(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {

		String requestUrl = wbRequest.get(WaybackConstants.REQUEST_URL);
		return (requestUrl != null) && requestUrl.endsWith(".css");
	}

	public void renderException(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception) throws ServletException, IOException {

		// the "standard HTML" response handler:
		String finalJspPath = jspPath + "/" + ERROR_JSP;

		// try to not cause client errors by sending the HTML response if
		// this request is ebedded, and is obviously one of the special types:
		if (requestIsEmbedded(httpRequest, wbRequest)) {

			if (requestIsJavascript(httpRequest, wbRequest)) {

				finalJspPath = jspPath + "/" + ERROR_JAVASCRIPT;

			} else if (requestIsCSS(httpRequest, wbRequest)) {

				finalJspPath = jspPath + "/" + ERROR_CSS;

			} else if (requestIsImage(httpRequest, wbRequest)) {

				finalJspPath = jspPath + "/" + ERROR_IMAGE;

			}
		}

		httpRequest.setAttribute("exception", exception);
		UIResults uiResults = new UIResults(wbRequest);
		uiResults.storeInRequest(httpRequest);

		RequestDispatcher dispatcher = httpRequest
				.getRequestDispatcher(finalJspPath);

		dispatcher.forward(httpRequest, httpResponse);
	}

	/*  GENERIC RESPONSE HELPER METHODS:  */

	/**
	 * Send the raw bytes from is (presumably the Resource/ARCRecord) to
	 * os (presumably the clients/HTTPResponse's OutputStream) with no
	 * decoding. Send them all as-is.
	 * 
	 * @param is
	 * @param os 
	 * @throws IOException 
	 */
	protected void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[BYTE_BUFFER_SIZE];
		for (int r = -1; (r = is.read(buffer, 0, BYTE_BUFFER_SIZE)) != -1;) {
			os.write(buffer, 0, r);
		}
	}

	protected boolean isExactVersionRequested(WaybackRequest wbRequest,
			SearchResult result) {

		String reqDateStr = wbRequest.get(WaybackConstants.REQUEST_EXACT_DATE);
		String resDateStr = result.get(WaybackConstants.RESULT_CAPTURE_DATE);

		// some capture dates are not 14 digits, only compare as many
		// digits as are in the result date:
		return resDateStr.equals(reqDateStr.substring(0, resDateStr.length()));
	}

	/**
	 * test if the Resource and SearchResult should be replayed raw, without 
	 * any markup.
	 * 
	 * This version always indicates that the document should be returned raw,
	 * but is intended to be overriden.
	 * 
	 * @param resource
	 * @param result
	 * @return boolean, true if the document should be returned raw.
	 */
	protected boolean isRawReplayResult(Resource resource, 
			SearchResult result) {
		return true;
	}

	/**
	 * callback function for each HTTP header. If null is returned, header is
	 * omitted from final response to client, otherwise, the possibly modified
	 * http header value is returned to the client.
	 * 
	 * This version just hands back all headers transparently, but is intended
	 * to be overriden.
	 * 
	 * @param key
	 * @param value
	 * @param uriConverter
	 * @param result
	 * @return String
	 */
	protected String filterHeader(final String key, final String value,
			final ResultURIConverter uriConverter, SearchResult result) {
		return value;
	}

	/**
	 * Iterate over all HTTP headers in resource, possibly sending them on
	 * to the client. The determination as to omit, send as-is, or send modified
	 * is handled thru the overridable filterHeader() method.
	 * 
	 * @param response
	 * @param resource
	 * @param uriConverter
	 * @param result
	 * @throws IOException 
	 */
	protected void copyRecordHttpHeader(HttpServletResponse response,
			Resource resource, ResultURIConverter uriConverter,
			SearchResult result) throws IOException {
		Properties headers = resource.getHttpHeaders();
		int code = resource.getStatusCode();
		// Only return legit status codes -- don't return any minus
		// codes, etc.
		if (code <= HttpServletResponse.SC_CONTINUE) {
			String identifier = "";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Bad status code " + code + " (" + identifier + ").");
			return;
		}
		response.setStatus(code);
		if (headers != null) {
			for (Enumeration e = headers.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				String value = (String) headers.get(key);
				String finalValue = value;
				if (value != null) {
					finalValue = filterHeader(key, value, uriConverter, result);
					if (finalValue == null) {
						continue;
					}
				}
				response.setHeader(key, (finalValue == null) ? "" : finalValue);
			}
		}
	}

	/**
	 * Attempt to divine the character encoding of the document:
	 *   1) first try the chardet library,
	 *   2) then see if the Content-Type HTTP header helps (with a "charset=")
	 *   3) then assume... "UTF-8"
	 *   
	 * This should probably use the value from a META tag, if available, before
	 * #2 and #3..
	 * 
	 * @param resource
	 * @return String character set guessed
	 * @throws IOException 
	 */
	protected String getCharset(Resource resource) throws IOException {
		String charsetName = null;

		myICharsetDetectionObserver obs = new myICharsetDetectionObserver();
		

		nsDetector det = new nsDetector(nsPSMDetector.ALL);
		byte[] bbuffer = new byte[MAX_CHARSET_READAHEAD];
		det.Init(obs);

		resource.mark(MAX_CHARSET_READAHEAD);
		int len = resource.read(bbuffer, 0, MAX_CHARSET_READAHEAD);
		resource.reset();

		if (len != -1) {
			det.DoIt(bbuffer, len, false);
			if (obs.detected != null) {
				charsetName = obs.detected;
			}
		}
		if (charsetName == null) {
			Properties httpHeaders = resource.getHttpHeaders();
			String ctype = httpHeaders.getProperty(HTTP_CONTENT_TYPE_HEADER);
			if (ctype != null) {
				int offset = ctype.indexOf(CHARSET_TOKEN);
				if (offset != -1) {
					charsetName = ctype.substring(offset
							+ CHARSET_TOKEN.length());
				}
			}
		}
		if (charsetName == null) {
			charsetName = "UTF-8";
		}
		return charsetName;
	}

	protected String getCharset2(Resource resource) throws IOException {
		String charsetName = null;

		byte[] bbuffer = new byte[MAX_CHARSET_READAHEAD];
		   // (1)
	    UniversalDetector detector = new UniversalDetector(null);

	    // (2)
		resource.mark(MAX_CHARSET_READAHEAD);
		int len = resource.read(bbuffer, 0, MAX_CHARSET_READAHEAD);
		resource.reset();
		detector.handleData(bbuffer, 0, len);
		// (3)
		detector.dataEnd();
	    // (4)
	    charsetName = detector.getDetectedCharset();

	    // (5)
	    detector.reset();

		return charsetName;
	}
	
	/** 
	 * Do "stuff" to the StringBuilder page argument.
	 * 
	 * This version does nothing at all, but is intended to be overridden.
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

	}


	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayRenderer#renderResource(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResult, org.archive.wayback.core.Resource, org.archive.wayback.ResultURIConverter)
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		if (resource == null) {
			throw new IllegalArgumentException("No resource");
		}
		if (result == null) {
			throw new IllegalArgumentException("No result");
		}

		if (isRawReplayResult(resource,result)) {

			resource.parseHeaders();
			copyRecordHttpHeader(httpResponse, resource, uriConverter, result);
			copy(resource, httpResponse.getOutputStream());

		} else {

			// We're going to do some markup on the page.
			// first we'll need to convert the bytes to a String, which
			// includes character encoding detection, then we'll call into the
			// overridable markUpPage(), then we'll convert back to bytes and 
			// return them to the client

			resource.parseHeaders();
			copyRecordHttpHeader(httpResponse, resource, uriConverter, result);

			int recordLength = (int) resource.getRecordLength();

			// get the charset:
			String charSet = getCharset2(resource);
			if(charSet == null) {
				charSet = getCharset(resource);
			}

			// convert bytes to characters for charset:
			InputStreamReader isr = new InputStreamReader(resource, charSet);

			char[] cbuffer = new char[C_BUFFER_SIZE];

			// slurp the whole thing into RAM:
			StringBuilder sbuffer = new StringBuilder(recordLength);
			for (int r = -1; (r = isr.read(cbuffer, 0, C_BUFFER_SIZE)) != -1;) {
				sbuffer.append(cbuffer, 0, r);
			}

			// do the "usual" markup:
			markUpPage(sbuffer, httpRequest, httpResponse, wbRequest, result,
					resource, uriConverter);

			// back to bytes...
			byte[] ba = sbuffer.toString().getBytes(charSet);

			// inform browser how much is coming back:
			httpResponse.setHeader("Content-Length", String.valueOf(ba.length));

			// and send it out the door...
			ServletOutputStream out = httpResponse.getOutputStream();
			out.write(ba);
		}
	}
	private class myICharsetDetectionObserver 
		implements nsICharsetDetectionObserver {

		/**
		 * null or the charset found
		 */
		public String detected = null;
		public void Notify(String detectedCharset) {
			detected = detectedCharset;
		}
	};
}
