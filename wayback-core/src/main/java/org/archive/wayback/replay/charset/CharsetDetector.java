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
package org.archive.wayback.replay.charset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Iterator;
import java.util.Map;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.TagMagix;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * Abstract class containing common methods for determining the character 
 * encoding of a text Resource, most of which should be refactored into a
 * Util package.
 * @author brad
 *
 */
public abstract class CharsetDetector {
	// hand off this many bytes to the chardet library
	protected final static int MAX_CHARSET_READAHEAD = 65536;
	// ...if it also includes "charset="
	protected final static String CHARSET_TOKEN = "charset=";
	// ...and if the chardet library fails, use the Content-Type header
	protected final static String HTTP_CONTENT_TYPE_HEADER = "Content-Type";
	/** the default charset name to use when giving up */
	public final static String DEFAULT_CHARSET = "UTF-8";

	protected boolean isCharsetSupported(String charsetName) {
		// can you believe that this throws a runtime? Just asking if it's
		// supported!!?! They coulda just said "no"...
		if(charsetName == null) {
			return false;
		}
		try {
			return Charset.isSupported(charsetName);
		} catch(IllegalCharsetNameException e) {
			return false;
		}
	}
	protected String mapCharset(String orig) {
		String lc = orig.toLowerCase();
		if(lc.contains("iso8859-1") || lc.contains("iso-8859-1")) {
			return "cp1252";
		}
		if(lc.contains("unicode")) {
			return DEFAULT_CHARSET;
		}
		return orig;
	}
	protected String contentTypeToCharset(final String contentType) {
		int offset = 
				contentType.toUpperCase().indexOf(CHARSET_TOKEN.toUpperCase());

		if (offset != -1) {
			String cs = contentType.substring(offset + CHARSET_TOKEN.length());
			if(isCharsetSupported(cs)) {
				return mapCharset(cs);
			}
			// test for extra spaces... there's at least one page out there that
			// indicates it's charset with:

			//  <meta http-equiv="Content-type" content="text/html; charset=i so-8859-1">

			// bad web page!
			if(isCharsetSupported(cs.replace(" ", ""))) {
				return mapCharset(cs.replace(" ", ""));
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
		Iterator<String> keys = httpHeaders.keySet().iterator();
		String ctype = null;
		while(keys.hasNext()) {
			String headerKey = keys.next();
			String keyCmp = headerKey.toUpperCase().trim();
			if(keyCmp.equals(HTTP_CONTENT_TYPE_HEADER.toUpperCase())) {
				ctype = httpHeaders.get(headerKey);
				break;
			}
		}
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
	protected String getCharsetFromMeta(InputStream resource) throws IOException {
		String charsetName = null;

		byte[] bbuffer = new byte[MAX_CHARSET_READAHEAD];
		resource.mark(MAX_CHARSET_READAHEAD);
		resource.read(bbuffer, 0, MAX_CHARSET_READAHEAD);
		resource.reset();
		// convert to UTF-8 String -- which hopefully will not mess up the
		// characters we're interested in...
		StringBuilder sb = new StringBuilder(new String(bbuffer,DEFAULT_CHARSET));
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
	protected String getCharsetFromBytes(InputStream resource) throws IOException {
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
		if(isCharsetSupported(charsetName)) {
			return charsetName;
		}
		return null;
	}

	public String getCharset(Resource resource, WaybackRequest request)
			throws IOException {
		return getCharset(resource, resource, request);
	}

	/**
	 * @param httpHeadersResource resource with http headers to consider 
	 * @param payloadResource resource with payload to consider (presumably text)
	 * @param request WaybackRequest which may contain additional hints to
	 *        processing
	 * @return String charset name for the Resource
	 * @throws IOException if there are problems reading the Resource
	 */
	public abstract String getCharset(Resource httpHeadersResource,
			Resource payloadResource, WaybackRequest wbRequest)
					throws IOException;
}
