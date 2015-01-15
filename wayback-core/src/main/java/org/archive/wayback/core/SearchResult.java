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
package org.archive.wayback.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.archive.wayback.util.Timestamp;

/**
 *
 *
 * @author brad
 */
public class SearchResult {

	public static final String RESULT_TRUE_VALUE = "true";

	public static final String CUSTOM_HEADER_PREFIX = "custom.http.header.";

	/**
	 * Expandable Data bag for String to String tuples -- who knows what data
	 * we'll want to put in an Index. Perhaps this should BE a Properties,
	 * instead of HAVEing a Properties.. This way, we could add an extra, 'type'
	 * field that would allow discrimination/hinting at what kind of data might
	 * be found in the Properties...
	 */
	protected HashMap<String, String> data = null;

	public SearchResult() {
		data = new HashMap<String, String>();
	}

	protected SearchResult(boolean autocreateMap) {
		if (autocreateMap) {
			data = new HashMap<String, String>();
		}
	}

	protected void ensureMap() {
		if (data == null) {
			data = new HashMap<String, String>();
		}
	}

	protected String get(String key) {
		return data.get(key);
	}

	protected void put(String key, String value) {
		data.put(key, value);
	}

	// Explicitly for external/custom properties, ensure map is created
	public void putCustom(String key, String value) {
		ensureMap();
		put(key, value);
	}

	public String getCustom(String key) {
		ensureMap();
		return get(key);
	}

	protected boolean getBoolean(String key) {
		String value = get(key);
		return (value != null && value.equals(RESULT_TRUE_VALUE));
	}

	protected void putBoolean(String key, boolean value) {
		if (value) {
			put(key, RESULT_TRUE_VALUE);
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
