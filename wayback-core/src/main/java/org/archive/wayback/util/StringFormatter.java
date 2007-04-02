/* StringFormatter
 *
 * $Id$
 *
 * Created on 5:22:12 PM Jan 31, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StringFormatter {

	ResourceBundle bundle = null;
	Locale locale = null;
	Map<String,MessageFormat> formats = null;
	/**
	 * @param bundle
	 * @param locale
	 */
	public StringFormatter(ResourceBundle bundle, Locale locale) {
		this.bundle = bundle;
		this.locale = locale;
		formats = new HashMap<String,MessageFormat>();
	}
	
	private MessageFormat getFormat(String pattern) {
		MessageFormat format = formats.get(pattern);
		if(format == null) {
			format = new MessageFormat(pattern,locale);
			formats.put(pattern,format);
		}
		return format;
	}
	
	/**
	 * @param key
	 * @return localized String version of key argument, or key itself if
	 * something goes wrong...
	 */
	public String getLocalized(String key) {
		try {
			return bundle.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	/**
	 * @param key
	 * @param args
	 * @return Localized String for key, interpolated with args
	 */
	public String format(String key, Object args[]) {
		try {
		return getFormat(getLocalized(key)).format(args);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return key;
	}
	/**
	 * @param key
	 * @return Localized String for key
	 */
	public String format(String key) {
		Object args[] = {};
		return format(key,args);
	}
	/**
	 * @param key
	 * @param o1
	 * @return Localized String for key, interpolated with o1
	 */
	public String format(String key,Object o1) {
		Object args[] = {o1};
		return format(key,args);
	}
	/**
	 * @param key
	 * @param o1
	 * @param o2
	 * @return Localized String for key, interpolated with o1,o2
	 */
	public String format(String key,Object o1,Object o2) {
		Object args[] = {o1,o2};
		return format(key,args);
	}
	/**
	 * @param key
	 * @param o1
	 * @param o2
	 * @param o3
	 * @return Localized String for key, interpolated with o1,o2,o3
	 */
	public String format(String key,Object o1,Object o2,Object o3) {
		Object args[] = {o1,o2,o3};
		return format(key,args);
	}
	/**
	 * @param key
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @return Localized String for key, interpolated with o1,o2,o3,o4
	 */
	public String format(String key,Object o1,Object o2,Object o3,Object o4) {
		Object args[] = {o1,o2,o3,o4};
		return format(key,args);
	}
}
