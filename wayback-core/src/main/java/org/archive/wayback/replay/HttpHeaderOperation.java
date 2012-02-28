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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.CaptureSearchResult;
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
	 * @return
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

}
