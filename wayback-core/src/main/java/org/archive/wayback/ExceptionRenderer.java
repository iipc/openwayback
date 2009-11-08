/* ExceptionRenderer
 *
 * $Id$
 *
 * Created on 6:26:05 PM Jun 10, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
package org.archive.wayback;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

/**
 * Implementors are responsible for drawing errors.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface ExceptionRenderer {
	/**
	 * Render the contents of a WaybackException in either html, javascript, or
	 * css format, depending on the guessed context, so errors in embedded 
	 * documents do not cause unneeded errors in the embedding document.
	 * 
	 * @param httpRequest from Servlet handling
	 * @param httpResponse from Servlet handling
	 * @param wbRequest as parsed by RequestParser
	 * @param exception specific WaybackException subclass thrown
	 * @param uriConverter for the AccessPoint handling the request
	 * @throws ServletException per usual
	 * @throws IOException per usual
	 */
	public void renderException(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception, ResultURIConverter uriConverter)
		throws ServletException, IOException;
}
