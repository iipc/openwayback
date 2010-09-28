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
