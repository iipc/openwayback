/* SearchResult
 *
 * $Id$
 *
 * Created on 12:45:18 PM Nov 9, 2005.
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
package org.archive.wayback.core;

import java.util.Properties;

import org.archive.wayback.WaybackConstants;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResult {
	/**
	 * Expandable Data bag for String to String tuples -- who knows what data
	 * we'll want to put in an Index. Perhaps this should BE a Properties,
	 * instead of HAVEing a Properties.. This way, we could add an extra, 
	 * 'type' field that would allow discrimination/hinting at what kind
	 * of data might be found in the Properties...
	 */
	private Properties data = null;
	
	/**
	 * Constructor
	 */
	public SearchResult() {
		super();
		data = new Properties();
	}
	
	/**
	 * @param key
	 * @return boolean true if 'key' is a key in 'data'
	 */
	public boolean containsKey(String key) {
		return data.containsKey(key);
	}

	/**
	 * @param key
	 * @return String value for key 'key' -- null if 'key' does not exist
	 */
	public String get(String key) {
		return (String) data.get(key);
	}

	/**
	 * @param key
	 * @param value
	 * @return String previous value of 'key'
	 */
	public String put(String key, String value) {
		return (String) data.put(key, value);
	}

	/**
	 * @return Returns the data.
	 */
	public Properties getData() {
		return data;
	}

	/**
	 * @return the (probably) 14-digit timestamp indicating when this capture
	 * was made.
	 */
	public String getCaptureDate() {
		return get(WaybackConstants.RESULT_CAPTURE_DATE);
	}
	
	/**
	 * @return the url that created this request, without the leading http://
	 */
	public String getUrl() {
		return get(WaybackConstants.RESULT_URL);
	}

	/**
	 * @return the url that created this request, including the leading http://
	 */
	public String getAbsoluteUrl() {
		String url = get(WaybackConstants.RESULT_URL);
		if(url.startsWith(WaybackConstants.HTTP_URL_PREFIX)) {
			return url;
		}
		return WaybackConstants.HTTP_URL_PREFIX + url;
	}
}
