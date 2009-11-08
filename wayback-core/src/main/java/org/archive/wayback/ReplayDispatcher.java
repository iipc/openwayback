/* ReplayDispatcher
 *
 * $Id$
 *
 * Created on 6:10:18 PM Aug 9, 2007.
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
package org.archive.wayback;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.WaybackRequest;

/**
 * Locate and return a ReplayRenderer appropriate for the users request 
 * (accept header, for example) and the resulting Resource.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface ReplayDispatcher {
	/**
	 * 
	 * Return a ReplayRenderer appropriate for the Resource.
	 * 
	 * @param wbRequest WaybackRequest being handled.
	 * @param result CapturSearchResult from the ResourceIndex which is
	 * 				 being returned.
	 * @param resource Resource as returned by ResourceStore which should
	 *               be returned to the user.
	 * @return an appropriate ReplayRenderer for the Resource, given the request
	 *         context
	 */
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource);
}
