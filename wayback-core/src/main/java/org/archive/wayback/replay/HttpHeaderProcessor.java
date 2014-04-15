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

import java.util.Map;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface HttpHeaderProcessor {

	// TODO: consolidate constants with org.archive.wayback.replay.HttpHeaderOperation

	public final static String HTTP_LENGTH_HEADER = "Content-Length";
	public final static String HTTP_LENGTH_HEADER_UP = 
		HTTP_LENGTH_HEADER.toUpperCase();

	public final static String HTTP_LOCATION_HEADER = "Location";
	public final static String HTTP_LOCATION_HEADER_UP = 
		HTTP_LOCATION_HEADER.toUpperCase();

	public final static String HTTP_CONTENT_BASE_HEADER = "Content-Base";
	public final static String HTTP_CONTENT_BASE_HEADER_UP = 
		HTTP_CONTENT_BASE_HEADER.toUpperCase();

	public final static String HTTP_CONTENT_LOCATION_HEADER = "Content-Location";
	public final static String HTTP_CONTENT_LOCATION_HEADER_UP = 
		HTTP_CONTENT_LOCATION_HEADER.toUpperCase();

	public final static String HTTP_CONTENT_TYPE_HEADER = "Content-Type";
	public final static String HTTP_CONTENT_TYPE_HEADER_UP = 
		HTTP_CONTENT_TYPE_HEADER.toUpperCase();

	public final static String HTTP_CONTENT_DISP_HEADER = "Content-Disposition";
	public final static String HTTP_CONTENT_DISP_HEADER_UP = 
		HTTP_CONTENT_DISP_HEADER.toUpperCase();

	public final static String HTTP_TRANSFER_ENCODING_HEADER = "Transfer-Encoding";
	/**
	 * Transfer-Encoding in all-uppercase
	 */
	public final static String HTTP_TRANSFER_ENCODING_HEADER_UP =
			HTTP_TRANSFER_ENCODING_HEADER.toUpperCase();
	
	/**
	 * optionally add header key:value to output for later returning to client
	 * 
	 * @param output
	 * @param key
	 * @param value
	 * @param uriConverter 
	 * @param result 
	 */
	public void filter(Map<String,String> output, String key, String value,
			final ResultURIConverter uriConverter, CaptureSearchResult result);
}
