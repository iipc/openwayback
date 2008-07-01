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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResult {

	public static final String RESULT_TRUE_VALUE = "true";

	/**
	 * Expandable Data bag for String to String tuples -- who knows what data
	 * we'll want to put in an Index. Perhaps this should BE a Properties,
	 * instead of HAVEing a Properties.. This way, we could add an extra, 
	 * 'type' field that would allow discrimination/hinting at what kind
	 * of data might be found in the Properties...
	 */
	protected HashMap<String,String> data = null;

	public SearchResult() {
		data = new HashMap<String,String>();
	}
	public String get(String key) {
		return data.get(key);
	}
	public void put(String key, String value) {
		data.put(key,value);
	}
	public boolean getBoolean(String key) {
		String value = get(key);
		return (value != null && value.equals(RESULT_TRUE_VALUE));
	}
	public void putBoolean(String key, boolean value) {
		if(value) {
			put(key,RESULT_TRUE_VALUE);
		} else {
			data.remove(key);
		}
	}
	protected String dateToTS(Date date) {
		return new Timestamp(date).getDateStr();
	}
	protected Date tsToDate(String timestamp) {
		return Timestamp.parseBefore(timestamp).getDate();
	}
	public Map<String, String> toCanonicalStringMap() {
		return data;
	}
	public void fromCanonicalStringMap(Map<String, String> canonical) {
		data = new HashMap<String, String>();
		data.putAll(canonical);
	}
}
