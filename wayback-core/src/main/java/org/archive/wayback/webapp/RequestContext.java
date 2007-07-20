/* RequestContext
 *
 * $Id$
 *
 * Created on 4:46:47 PM Jul 19, 2007.
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface RequestContext {
	/**
	 * @param httpRequest
	 * @param httpResponse
	 * @return true if the RequestContent returned data to the client.
	 * @throws ServletException 
	 * @throws IOException 
	 */
	public boolean handleRequest(HttpServletRequest httpRequest, 
			HttpServletResponse httpResponse)
	throws ServletException, IOException;
}
