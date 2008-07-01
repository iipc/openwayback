/* HeaderFilter
 *
 * $Id$
 *
 * Created on 6:41:12 PM Aug 8, 2007.
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

	public final static String HTTP_LENGTH_HEADER = "Content-Length";
	public final static String HTTP_LENGTH_HEADER_UP = 
		HTTP_LENGTH_HEADER.toUpperCase();

	public final static String HTTP_LOCATION_HEADER = "Location";
	public final static String HTTP_LOCATION_HEADER_UP = 
		HTTP_LOCATION_HEADER.toUpperCase();

	public final static String HTTP_CONTENT_BASE_HEADER = "Content-Base";
	public final static String HTTP_CONTENT_BASE_HEADER_UP = 
		HTTP_CONTENT_BASE_HEADER.toUpperCase();

	public final static String HTTP_CONTENT_TYPE_HEADER = "Content-Type";
	public final static String HTTP_CONTENT_TYPE_HEADER_UP = 
		HTTP_CONTENT_TYPE_HEADER.toUpperCase();
	
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
