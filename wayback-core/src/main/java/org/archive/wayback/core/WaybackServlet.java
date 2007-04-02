/* WaybackServlet
 *
 * $Id$
 *
 * Created on 4:42:13 PM May 9, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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

import java.text.ParseException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Base clas for HttpServlets that have the "project standard" WaybackLogic
 * factory for accessing other components.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WaybackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected WaybackLogic wayback = new WaybackLogic();
	
	/**
	 * generic constructor.
	 */
	public WaybackServlet() {
		super();
	}
	
	public void init(ServletConfig c) throws ServletException {

		Properties p = new Properties();
		ServletContext sc = c.getServletContext();
		for (Enumeration e = sc.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, sc.getInitParameter(key));
		}
		for (Enumeration e = c.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, c.getInitParameter(key));
		}
		
		wayback.init(p);
	}
	
	protected static String getMapParam(Map queryMap, String field) {
		String arr[] = (String[]) queryMap.get(field);
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}

	protected static String getRequiredMapParam(Map queryMap, String field)
	throws ParseException {
		String value = getMapParam(queryMap,field);
		if(value == null) {
			throw new ParseException("missing field " + field,0);
		}
		if(value.length() == 0) {
			throw new ParseException("empty field " + field,0);			
		}
		return value;
	}

	protected static String getMapParamOrEmpty(Map map, String param) {
		String val = getMapParam(map,param);
		return (val == null) ? "" : val;
	}	
}
