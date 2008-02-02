/* HTMLPage
 *
 * $Id$
 *
 * Created on 12:39:52 PM Aug 7, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.replay;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.query.UIQueryResults;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * Class which wraps functionality for converting a Resource(InputStream + 
 * HTTP headers) into a StringBuilder, performing several common URL 
 * resolution methods against that StringBuilder, inserting arbitrary Strings
 * into the page, and then converting the page back to a byte array. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class HTMLPage {

	// hand off this many bytes to the chardet library
	private final static int MAX_CHARSET_READAHEAD = 65536;
	// ...if it also includes "charset="
	private final static String CHARSET_TOKEN = "charset=";
	// ...and if the chardet library fails, use the Content-Type header
	private final static String HTTP_CONTENT_TYPE_HEADER = "Content-Type";
	// if documents are marked up before sending to clients, the data is
	// decoded into a String in chunks. This is how big a chunk to decode with.
	private final static int C_BUFFER_SIZE = 4096;

	private Resource resource = null;
	private SearchResult result = null; 
	private ResultURIConverter uriConverter = null;
	/**
	 * the internal StringBuilder
	 */
	public StringBuilder sb = null;
	private String charSet = null;
	private byte[] resultBytes = null;

	/**
	 * @param resource
	 * @param result
	 * @param uriConverter 
	 */
	public HTMLPage(Resource resource, SearchResult result, 
			ResultURIConverter uriConverter) {
		this.resource = resource;
		this.result = result;
		this.uriConverter = uriConverter;
	}

	private boolean isCharsetSupported(String charsetName) {
		// can you believe that this throws a runtime? Just asking if it's
		// supported!!?! They coulda just said "no"...
		try {
			return Charset.isSupported(charsetName);
		} catch(IllegalCharsetNameException e) {
			return false;
		}
	}
	
	private String contentTypeToCharset(final String contentType) {
		int offset = contentType.indexOf(CHARSET_TOKEN);
		if (offset != -1) {
			String cs = contentType.substring(offset + CHARSET_TOKEN.length());
			if(isCharsetSupported(cs)) {
				return cs;
			}
			// test for extra spaces... there's at least one page out there that
			// indicates it's charset with:

//  <meta http-equiv="Content-type" content="text/html; charset=i so-8859-1">

			// bad web page!
			if(isCharsetSupported(cs.replace(" ", ""))) {
				return cs.replace(" ", "");
			}
		}
		return null;
	}
	
	/**
	 * Attempt to divine the character encoding of the document from the 
	 * Content-Type HTTP header (with a "charset=")
	 * 
	 * @param resource
	 * @return String character set found or null if the header was not present
	 * @throws IOException 
	 */
	protected String getCharsetFromHeaders(Resource resource) 
	throws IOException {
		
		String charsetName = null;

		Map<String,String> httpHeaders = resource.getHttpHeaders();
		String ctype = httpHeaders.get(HTTP_CONTENT_TYPE_HEADER);
		if (ctype != null) {
			charsetName = contentTypeToCharset(ctype);
		}
		return charsetName;
	}

	/**
	 * Attempt to find a META tag in the HTML that hints at the character set
	 * used to write the document.
	 * 
	 * @param resource
	 * @return String character set found from META tags in the HTML
	 * @throws IOException
	 */
	protected String getCharsetFromMeta(Resource resource) throws IOException {
		String charsetName = null;

		byte[] bbuffer = new byte[MAX_CHARSET_READAHEAD];
		resource.mark(MAX_CHARSET_READAHEAD);
		resource.read(bbuffer, 0, MAX_CHARSET_READAHEAD);
		resource.reset();
		// convert to UTF-8 String -- which hopefully will not mess up the
		// characters we're interested in...
		StringBuilder sb = new StringBuilder(new String(bbuffer,"UTF-8"));
		String metaContentType = TagMagix.getTagAttrWhere(sb, "META",
				"content", "http-equiv", "Content-Type");
		if(metaContentType != null) {
			charsetName = contentTypeToCharset(metaContentType);
		}
		return charsetName;
	}
	
	/**
	 * Attempts to figure out the character set of the document using
	 * the excellent juniversalchardet library.
	 * 
	 * @param resource
	 * @return String character encoding found, or null if nothing looked good.
	 * @throws IOException
	 */
	protected String getCharsetFromBytes(Resource resource) throws IOException {
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
	 * Use META tags, byte-character-detection, HTTP headers, hope, and prayer
	 * to figure out what character encoding is being used for the document.
	 * If nothing else works, assumes UTF-8 for now.
	 * 
	 * @param resource
	 * @return String charset for Resource
	 * @throws IOException
	 */
	protected String guessCharset() throws IOException {
		
		String charSet = getCharsetFromMeta(resource);
		if(charSet == null) {
			charSet = getCharsetFromBytes(resource);
			if(charSet == null) {
				charSet = getCharsetFromHeaders(resource);
				if(charSet == null) {
					charSet = "UTF-8";
				}
			}
		}
		return charSet;
	}

	/**
	 * Update URLs inside the page, so those URLs which must be correct at
	 * page load time resolve correctly to absolute URLs.
	 * 
	 * This means ensuring there is a BASE HREF tag, adding one if missing,
	 * and then resolving:
	 *     FRAME-SRC, META-URL, LINK-HREF, SCRIPT-SRC
	 * tag-attribute pairs against either the existing BASE-HREF, or the
	 * page's absolute URL if it was missing.
	 */
	public void resolvePageUrls() {

		// TODO: get url from Resource instead of SearchResult?
		String pageUrl = result.getAbsoluteUrl();
		String captureDate = result.getCaptureDate();

		String existingBaseHref = TagMagix.getBaseHref(sb);
		if (existingBaseHref == null) {
			insertAtStartOfHead("<base href=\"" + pageUrl + "\" />");
		} else {
			pageUrl = existingBaseHref;
		}

		String markups[][] = {
				{"FRAME","SRC"},
				{"META","URL"},
				{"LINK","HREF"},
				{"SCRIPT","SRC"},
				{TagMagix.ANY_TAGNAME,"background"}
		};
		// TODO: The classic WM added a js_ to the datespec, so NotInArchives
		// can return an valid javascript doc, and not cause Javascript errors.
		for(String tagAttr[] : markups) {
			TagMagix.markupTagREURIC(sb, uriConverter, captureDate, pageUrl,
					tagAttr[0], tagAttr[1]);
		}
		TagMagix.markupCSSImports(sb,uriConverter, captureDate, pageUrl);
		TagMagix.markupStyleUrls(sb,uriConverter,captureDate,pageUrl);
	}
	
	/**
	 * Update all URLs inside the page, so they resolve correctly to absolute 
	 * URLs within the Wayback service.
	 */
	public void resolveAllPageUrls() {

		// TODO: get url from Resource instead of SearchResult?
		String pageUrl = result.getAbsoluteUrl();
		String captureDate = result.getCaptureDate();

		String existingBaseHref = TagMagix.getBaseHref(sb);
		if (existingBaseHref != null) {
			pageUrl = existingBaseHref;
		}
		ResultURIConverter ruc = new SpecialResultURIConverter(uriConverter);
		
		// TODO: forms...?
		String markups[][] = {
				{"FRAME","SRC"},
				{"META","URL"},
				{"LINK","HREF"},
				{"SCRIPT","SRC"},
				{"IMG","SRC"},
				{"A","HREF"},
				{"AREA","HREF"},
				{"OBJECT","CODEBASE"},
				{"OBJECT","CDATA"},
				{"APPLET","CODEBASE"},
				{"APPLET","ARCHIVE"},
				{"EMBED","SRC"},
				{"IFRAME","SRC"},
				{TagMagix.ANY_TAGNAME,"background"}
		};
		for(String tagAttr[] : markups) {
			TagMagix.markupTagREURIC(sb, ruc, captureDate, pageUrl,
					tagAttr[0], tagAttr[1]);
		}
		TagMagix.markupCSSImports(sb,uriConverter, captureDate, pageUrl);
		TagMagix.markupStyleUrls(sb,uriConverter,captureDate,pageUrl);
	}
	
	public void resolveCSSUrls() {
		// TODO: get url from Resource instead of SearchResult?
		String pageUrl = result.getAbsoluteUrl();
		String captureDate = result.getCaptureDate();
		TagMagix.markupCSSImports(sb,uriConverter, captureDate, pageUrl);
	}

	/**
	 * @param charSet
	 * @throws IOException 
	 */
	public void readFully(String charSet) throws IOException {
		if(charSet == null) {
			charSet = guessCharset();
		}
		this.charSet = charSet;
		int recordLength = (int) resource.getRecordLength();

		// convert bytes to characters for charset:
		InputStreamReader isr = new InputStreamReader(resource, charSet);

		char[] cbuffer = new char[C_BUFFER_SIZE];

		// slurp the whole thing into RAM:
		sb = new StringBuilder(recordLength);
		for (int r = -1; (r = isr.read(cbuffer, 0, C_BUFFER_SIZE)) != -1;) {
			sb.append(cbuffer, 0, r);
		}
	}
		
	/**
	 * Read bytes from input stream, using best-guess for character encoding
	 * @throws IOException 
	 */
	public void readFully() throws IOException {
		readFully(null);
	}
	
	/**
	 * @return raw bytes contained in internal StringBuilder
	 * @throws UnsupportedEncodingException
	 */
	public byte[] getBytes() throws UnsupportedEncodingException {
		if(sb == null) {
			throw new IllegalStateException("No interal StringBuffer");
		}
		if(resultBytes == null) {
			resultBytes = sb.toString().getBytes(charSet);
		}
		return resultBytes;
	}
	
	/**
	 * Write the contents of the page to the client.
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void writeToOutputStream(OutputStream os) throws IOException {
		if(sb == null) {
			throw new IllegalStateException("No interal StringBuffer");
		}
		byte[] b;
		try {
			b = getBytes();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		os.write(b);
	}

	/**
	 * @param toInsert
	 */	
	public void insertAtStartOfHead(String toInsert) {
		int insertPoint = TagMagix.getEndOfFirstTag(sb,"head");
		if (-1 == insertPoint) {
			insertPoint = 0;
		}
		sb.insert(insertPoint,toInsert);
	}

	/**
	 * @param toInsert
	 */
	public void insertAtEndOfBody(String toInsert) {
		int insertPoint = sb.lastIndexOf("</body>");
		if (-1 == insertPoint) {
			insertPoint = sb.lastIndexOf("</BODY>");
		}
		if (-1 == insertPoint) {
			insertPoint = sb.length();
		}
		sb.insert(insertPoint,toInsert);
	}
	/**
	 * @param toInsert
	 */
	public void insertAtStartOfBody(String toInsert) {
		int insertPoint = TagMagix.getEndOfFirstTag(sb,"body");
		if (-1 == insertPoint) {
			insertPoint = 0;
		}
		sb.insert(insertPoint,toInsert);
	}	
	/**
	 * @param jspPath
	 * @param httpRequest
	 * @param httpResponse
	 * @param wbRequest
	 * @param results
	 * @return
	 * @throws IOException 
	 * @throws ServletException 
	 * @throws ParseException 
	 */
	public String includeJspString(String jspPath, 
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, SearchResults results, SearchResult result) 
	throws ServletException, IOException {
		
		UIQueryResults uiResults = new UIQueryResults(httpRequest, wbRequest,
				results, uriConverter);
		uiResults.setResult(result);

		StringHttpServletResponseWrapper wrappedResponse = 
			new StringHttpServletResponseWrapper(httpResponse);
		uiResults.storeInRequest(httpRequest,jspPath);
		RequestDispatcher dispatcher = httpRequest.getRequestDispatcher(jspPath);
		dispatcher.forward(httpRequest, wrappedResponse);
		return wrappedResponse.getStringResponse();
	}
	
	/**
	 * @param jsUrl
	 * @return
	 */
	public String getJSIncludeString(final String jsUrl) {
		return "<script type=\"text/javascript\" src=\"" 
			+ jsUrl + "\" ></script>\n";
	}

	/**
	 * @return the charSet
	 */
	public String getCharSet() {
		return charSet;
	}

	/**
	 * @param charSet the charSet to set
	 */
	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}

	private class SpecialResultURIConverter implements ResultURIConverter {
		private static final String EMAIL_PROTOCOL_PREFIX = "mailto:";
		private static final String JAVASCRIPT_PROTOCOL_PREFIX = "javascript:";
		private ResultURIConverter base = null;
		public SpecialResultURIConverter(ResultURIConverter base) {
			this.base = base;
		}
		public String makeReplayURI(String datespec, String url) {
			if(url.startsWith(EMAIL_PROTOCOL_PREFIX)) {
				return url;
			}
			if(url.startsWith(JAVASCRIPT_PROTOCOL_PREFIX)) {
				return url;
			}
			return base.makeReplayURI(datespec, url);
		}
	}
}
