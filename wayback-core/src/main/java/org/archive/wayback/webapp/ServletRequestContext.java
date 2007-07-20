/* ServletRequestContext
 *
 * $Id$
 *
 * Created on 4:51:05 PM Jul 19, 2007.
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
package org.archive.wayback.webapp;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class ServletRequestContext implements RequestContext {

	protected static String getMapParam(Map<String,String[]> queryMap,
			String field) {
		String arr[] = (String[]) queryMap.get(field);
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}

	protected static String getRequiredMapParam(Map<String,String[]> queryMap,
			String field)
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

	protected static String getMapParamOrEmpty(Map<String,String[]> map,
			String param) {
		String val = getMapParam(map,param);
		return (val == null) ? "" : val;
	}	
	/* (non-Javadoc)
	 * @see org.archive.wayback.webapp.RequestContext#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public abstract boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException;

}
