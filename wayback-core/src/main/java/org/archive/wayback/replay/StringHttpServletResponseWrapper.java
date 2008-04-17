/* StringHttpServletResponseWrapper
 *
 * $Id$
 *
 * Created on 4:35:39 PM Aug 6, 2007.
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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StringHttpServletResponseWrapper extends HttpServletResponseWrapper {
	private final static String WRAPPED_CHAR_ENCODING = "UTF-8";
	private StringWriter sw = new StringWriter();
	private String origEncoding = null;
	
	/**
	 * @param response
	 */
	public StringHttpServletResponseWrapper(HttpServletResponse response) {
		super(response);
		origEncoding = getCharacterEncoding();
		setCharacterEncoding(WRAPPED_CHAR_ENCODING);
	}
	public PrintWriter getWriter() {
		return new PrintWriter(sw);
	}
	/**
	 * @return
	 */
	public String getStringResponse() {
		setCharacterEncoding(origEncoding);
		return sw.toString();
	}
}
