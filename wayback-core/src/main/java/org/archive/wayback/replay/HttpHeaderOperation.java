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
package org.archive.wayback.replay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.BadContentException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class HttpHeaderOperation {
	public final static String HTTP_LENGTH_HEADER = "Content-Length";
	public final static String HTTP_LENGTH_HEADER_UP = 
		HTTP_LENGTH_HEADER.toUpperCase();
	public final static String HTTP_TRANSFER_ENC_HEADER = 
		"Transfer-Encoding".toUpperCase();
	public final static String HTTP_CHUNKED_ENCODING_HEADER = 
		"chunked".toUpperCase();
	public final static String HTTP_CONTENT_ENCODING = "Content-Encoding";

	public static final String HTTP_RANGE_HEADER = "Range";
	public static final String HTTP_RANGE_HEADER_UP = HTTP_RANGE_HEADER
		.toUpperCase();
	public static final String HTTP_CONTENT_RANGE_HEADER = "Content-Range";
	public static final String HTTP_CONTENT_RANGE_HEADER_UP = HTTP_CONTENT_RANGE_HEADER
		.toUpperCase();

	/**
	 * @param resource
	 * @param httpResponse
	 * @throws BadContentException
	 */
	public static void copyHTTPMessageHeader(Resource resource, 
			HttpServletResponse httpResponse) throws BadContentException {

		// set status code from original resource (which will definitely confuse
		// many clients...)
		int code = resource.getStatusCode();
		// Only return legit status codes -- don't return any minus
		// codes, etc.
		if (code <= HttpServletResponse.SC_CONTINUE) {
			throw new BadContentException("Bad status code " + code);
		}
		httpResponse.setStatus(code);
	}
	
	/**
	 * @param resource
	 * @param result
	 * @param uriConverter
	 * @param filter 
	 * @return A HashMap containing the HTTP headers extracted from the Resource.
	 */
	public static Map<String,String> processHeaders(Resource resource, 
			CaptureSearchResult result, ResultURIConverter uriConverter, 
			HttpHeaderProcessor filter) {
		HashMap<String,String> output = new HashMap<String,String>();
		
		// copy all HTTP headers, as-is, sending "" instead of nulls.
		Map<String,String> headers = resource.getHttpHeaders();
		if (headers != null) {
			Iterator<String> itr = headers.keySet().iterator();
			while(itr.hasNext()) {
				String key = itr.next();
				String value = headers.get(key);
				value = (value == null) ? "" : value;
				filter.filter(output, key, value, uriConverter, result);
			}
		}
		return output;
	}

	/**
	 * @param headers
	 * @param response
	 */
	public static void sendHeaders(Map<String,String> headers, 
			HttpServletResponse response) {
		Iterator<String> itr = headers.keySet().iterator();
		while(itr.hasNext()) {
			String key = itr.next();
			String value = headers.get(key);
			value = (value == null) ? "" : value;
			response.setHeader(key,value);
		}
	}
	
	public static String getContentLength(Map<String,String> headers) {
		return getHeaderValue(headers,HTTP_LENGTH_HEADER);
	}
	public static boolean isChunkEncoded(Map<String,String> headers) {
		String enc = getHeaderValue(headers,HTTP_TRANSFER_ENC_HEADER);
		if(enc != null) {
			return enc.toUpperCase().contains(HTTP_CHUNKED_ENCODING_HEADER);
		}
		return false;
	}
	public static String getHeaderValue(Map<String,String> headers, String k) {
		String value = null;
		Iterator<String> itr = headers.keySet().iterator();
		String keyUp = k.toUpperCase();
		while(itr.hasNext()) {
			String key = itr.next();
			if(key != null) {
				if(key.toUpperCase().contains(keyUp)) {
					value = headers.get(key);
					break;
				}
			}
		}
		return value;
	}
	
	public static boolean removeHeader(Map<String,String> headers, String k) {
		Iterator<String> itr = headers.keySet().iterator();
		String keyUp = k.toUpperCase();
		while(itr.hasNext()) {
			String key = itr.next();
			if(key != null) {
				if (key.toUpperCase().contains(keyUp)) {
					itr.remove();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Replace header field {@code name} value with {@code value}, or
	 * add it if {@code headers} does not have {@code name}.
	 * @param headers header fields
	 * @param name header field name
	 * @param value new value for the header field
	 */
	public static void replaceHeader(Map<String, String> headers, String name, String value) {
		removeHeader(headers, name);
		headers.put(name, value);
	}

	/**
	 * Get {@code Range} header field, and return parsed ranges.
	 * @param headers header fields.
	 * @return array of long[2] or {@code null} if {@code Range} header
	 * field is not present, or invalid.
	 */
	public static long[][] getRange(Map<String, String> headers) {
		String rangeValue = getHeaderValue(headers, HTTP_RANGE_HEADER_UP);
		if (rangeValue == null) return null;
		rangeValue = rangeValue.trim();
		return parseRanges(rangeValue);
	}

	/**
	 * Parse {@code Range} header field value.
	 * Only {@code bytes} unit is supported.
	 * @param rangeValue {@code Range} header field value.
	 * @return an array of long[2], or {@code null} if invalid.
	 */
	public static long[][] parseRanges(String rangeValue) {
		if (!rangeValue.startsWith("bytes=")) return null;
		int s = "bytes=".length();
		int pcomma = rangeValue.indexOf(',', s);
		if (pcomma < 0) {
			long[] range = parseRange(rangeValue.substring(s));
			return range != null ? new long[][] { range } : null;
		}
		int e = rangeValue.length();
		List<long[]> ranges = new ArrayList<long[]>();
		while (true) {
			long[] range = parseRange(rangeValue.substring(s, pcomma));
			// TODO: too strict? fails on double comma.
			if (range == null) return null;
			ranges.add(range);
			if ((s = pcomma + 1) >= e) break;
			pcomma = rangeValue.indexOf(',', s);
			if (pcomma < 0) pcomma = e;
		}
		return ranges.toArray(new long[ranges.size()][]);
	}

	/**
	 * Parse single byte-range-spec.
	 * For suffix-byte-range-spec (-N), return [-N, -1].
	 * For byte-range-spec without last-byte-pos (M-), return [M, -1].
	 * For full-range-spec (M-N), return [M, N + 1].
	 * @param rangeValue byte-range-spec
	 * @return long[2], or {@code null} if invalid
	 */
	public static long[] parseRange(String rangeValue) {
		int phyphen = rangeValue.indexOf('-');
		if (phyphen == -1) {
			// this is a syntax error.
			return null;
		}
		try {
			if (phyphen == 0) {
				// -N (suffix-byte-range-spec)
				long end = Long.parseLong(rangeValue.substring(1));
				if (end <= 0) return null;
				return new long[] { -end, -1 };
			}
			if (phyphen == rangeValue.length() - 1) {
				// M- (byte-range-spec without last-byte-pos)
				long start = Long.parseLong(rangeValue.substring(0, phyphen));
				return new long[] { start, -1 };
			}
			// M-N (byte-range-spec)
			long start = Long.parseLong(rangeValue.substring(0, phyphen));
			long end = Long.parseLong(rangeValue.substring(phyphen + 1));
			if (start > end) return null;
			return new long[] { start, end + 1 };
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * Return {@code Content-Range} response header field value.
	 * @param headers response header fields.
	 * @return Three element array with <em>first-byte-pos</em>, <em>last-byte-pos + 1</em>
	 * and <em>instance-length</em>, or {@code null} if {@code value} is syntactically invalid.
	 */
	public static long[] getContentRange(Map<String, String> headers) {
		String value = getHeaderValue(headers, HTTP_CONTENT_RANGE_HEADER_UP);
		if (value == null) return null;
		return parseContentRange(value.trim());
	}

	/**
	 * Parse {@code value} as <em>byte-content-range-spec</em>.
	 * <em>byte-range-resp-spec</em> is parsed with {@link #parseRange(String)}.
	 * If <em>byte-range-resp-spec</em> is {@code *}, both <em>first-byte-pos</em> and
	 * <em>last-byte-pos</em> will be {@code -1}.
	 * @param value {@code Content-Range} header field value.
	 * @return Three element array with <em>first-byte-pos</em>, <em>last-byte-pos + 1</em>
	 * and <em>instance-length</em>, or {@code null} if {@code value} is syntactically invalid.
	 * @see #parseRange
	 */
	public static long[] parseContentRange(String value) {
		if (!value.startsWith("bytes ")) return null;
		int s = "bytes ".length();
		while (s < value.length() && value.charAt(s) == ' ')
			s++;
		int pslash = value.indexOf('/', s);
		if (pslash < 0) return null;

		final String rangeSpec = value.substring(s, pslash).trim();
		long start, stop;
		if (rangeSpec.equals("*"))
			start = stop = -1;
		else {
			long[] range = parseRange(rangeSpec);
			if (range == null) return null;
			if (range[0] < 0 || range[1] < 0) return null;
			start = range[0];
			stop = range[1];
		}
		final String instanceLength = value.substring(pslash + 1).trim();
		long length;
		if (instanceLength.equals("*"))
			length = -1;
		else {
			try {
				length = Long.parseLong(instanceLength);
			} catch (NumberFormatException ex) {
				return null;
			}
		}
		return new long[] { start, stop, length };
	}
}
