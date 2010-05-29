/* CharsetDetector
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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

public abstract class CharsetDetector {
	// hand off this many bytes to the chardet library
	protected final static int MAX_CHARSET_READAHEAD = 65536;
	// ...if it also includes "charset="
	protected final static String CHARSET_TOKEN = "charset=";
	// ...and if the chardet library fails, use the Content-Type header
	protected final static String HTTP_CONTENT_TYPE_HEADER = "Content-Type";
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
	
	protected String contentTypeToCharset(final String contentType) {
		int offset = 
			contentType.toUpperCase().indexOf(CHARSET_TOKEN.toUpperCase());
		
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
	public abstract String getCharset(Resource resource, WaybackRequest request)
		throws IOException;
}
