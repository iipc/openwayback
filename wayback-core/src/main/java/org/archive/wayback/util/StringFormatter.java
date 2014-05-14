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
package org.archive.wayback.util;

import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * An class which assists in UI generation, primarily through Locale-aware
 * String formatting, and also helps in escaping (hopefully properly) Strings
 * for use in HTML.
 * 
 * Note that date formatting done through this class forces all times to the 
 * UTC timezone - at the moment it appears too confusing to attempt to localize
 * times in any other way.. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StringFormatter {
	private final static TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
	
	final ResourceBundle bundle;
	final Locale locale;
	final Map<String,MessageFormat> formats;
	/**
	 * Construct a StringFormatter...
	 * @param bundle ResourceBundle to lookup patterns for MessageFormat 
	 * objects.
	 * @param locale to use, where applicable with MessageFormat objects
	 */
	public StringFormatter(ResourceBundle bundle) {
		this(null, Locale.getDefault());
	}
	/**
	 * Construct a StringFormatter...
	 * @param bundle ResourceBundle to lookup patterns for MessageFormat 
	 * objects.
	 * @param locale to use, where applicable with MessageFormat objects
	 */
	public StringFormatter(ResourceBundle bundle, Locale locale) {
		this.bundle = bundle;
		this.locale = locale;
		this.formats = new HashMap<String, MessageFormat>();
	}
	/**
	 * return {@code ResourceBundle} set to this object.
	 * (for migration to JSTL.)
	 * @return {@code ResourceBundle}
	 */
	public ResourceBundle getResourceBundle() {
		return bundle;
	}

	public MessageFormat getFormat(String pattern) {
		MessageFormat format = formats.get(pattern);
		if (format == null) {
			format = new MessageFormat(pattern,locale);
			// lets try to make sure any internal DateFormats use UTC:
			Format[] subFormats = format.getFormats();
			if (subFormats != null) {
				for (Format subFormat : subFormats) {
					if (subFormat instanceof DateFormat) {
						DateFormat subDateFormat = (DateFormat)subFormat;
						subDateFormat.setTimeZone(TZ_UTC);
					}
				}
			}
			formats.put(pattern, format);
		}
		return format;
	}
	
	/**
	 * Access a localized string associated with key from the ResourceBundle,
	 * likely the UI.properties file.
	 * @param key to lookup in the ResourceBundle
	 * @return localized String version of key argument, or key itself if
	 * something goes wrong...
	 */
	public String getLocalized(String key) {
		if (bundle != null) {
			try {
				return bundle.getString(key);
	//			String localized = bundle.getString(key);
	//			if((localized != null) && (localized.length() > 0)) {
	//				return localized;
	//			}
			} catch (Exception e) {
			}

		}
		return key;
	}

	private String formatInner(String key, Object objects[]) {
		try {
			return getFormat(getLocalized(key)).format(objects);
		} catch (Exception e) {
			// TODO: log message
			//e.printStackTrace();
		}
		return key;
	}
	
	// What gives? This works in the Junit test, but not in jsps...
//	/**
//	 * @param key String property name in UI.properties file to use as the 
//	 * pattern for interpolation
//	 * @param objects array of things to interpolate within the MessageFormat
//	 * 	described by the pattern in UI.properties for key key
//	 * @return Localized Formatted String for key, interpolated with args
//	 */
//	public String format(String key, Object...objects) {
//		return formatInner(key,objects);
//	}
	/**
	 * Localize a string key from the UI.properties file
	 * @param key String property name in UI.properties file to use as the 
	 * pattern for the MessageFormat
	 * @return Localized String for key
	 */
	public String format(String key) {
		Object args[] = {};
		return formatInner(key,args);
	}
	/**
	 * @param key String property name in UI.properties file to use as the 
	 * pattern for interpolation
	 * @param o1 thing1 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @return Localized Formatted String for key, interpolated with argument objects
	 */
	public String format(String key,Object o1) {
		Object args[] = {o1};
		return formatInner(key,args);
	}
	/**
	 * @param key String property name in UI.properties file to use as the 
	 * pattern for interpolation
	 * @param o1 thing1 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @param o2 thing2 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @return Localized Formatted String for key, interpolated with argument objects
	 */
	public String format(String key,Object o1,Object o2) {
		Object args[] = {o1,o2};
		return formatInner(key,args);
	}
	/**
	 * @param key String property name in UI.properties file to use as the 
	 * pattern for interpolation
	 * @param o1 thing1 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @param o2 thing2 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @param o3 thing3 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @return Localized Formatted String for key, interpolated with argument objects
	 */
	public String format(String key,Object o1,Object o2,Object o3) {
		Object args[] = {o1,o2,o3};
		return formatInner(key,args);
	}
	/**
	 * @param key String property name in UI.properties file to use as the 
	 * pattern for interpolation
	 * @param o1 thing1 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @param o2 thing2 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @param o3 thing3 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @param o4 thing4 to interpolate within the MessageFormat
	 * 	described by the pattern in UI.properties for key key
	 * @return Localized Formatted String for key, interpolated with argument objects
	 */
	public String format(String key,Object o1,Object o2,Object o3,Object o4) {
		Object args[] = {o1,o2,o3,o4};
		return formatInner(key,args);
	}

	/**
	 * handy shortcut to the Apache StringEscapeUtils.
	 * <p>Intended for JSP use. Otherwise use {@link StringEscapeUtils#escapeHtml(String)} instead.</p>
	 * @param raw string to be escaped
	 * @return the string escaped so it's safe for insertion in HTML
	 */
	public String escapeHtml(String raw) {
		return StringEscapeUtils.escapeHtml(raw);
	}
	/**
	 * handy shortcut to the Apache StringEscapeUtils.
	 * <p>Intended for JSP use. Otherwise use {@link StringEscapeUtils#escapeJavaScript(String)} instead.</p>
	 * @param raw string to be escaped
	 * @return the string escaped so it's safe for insertion in Javascript
	 */
	public String escapeJavaScript(String raw) {
		return StringEscapeUtils.escapeJavaScript(raw);
	}

	/**
	 * Convert... spaces to &nbsp;
	 * @param input to replace
	 * @return with spaces replaced
	 */
	public String spaceToNBSP(String input) {
		return input.replaceAll(" ", "&nbsp;");
	}
	
	public static String join(String delim, String...stuff) {
		int max = stuff.length - 1;
		int len = delim.length() * max;
		for (String s : stuff) {
			len += s.length();
		}
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < stuff.length; i++) {
			sb.append(stuff[i]);
			if (i < max) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}

}
