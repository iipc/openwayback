/* ResourceIndex
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback;

import java.io.IOException;

import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;

/**
 * Transforms a WMRequest into a ResourceResults.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public interface ResourceIndex {
	/**
	 * Transform a WMRequest into a ResourceResults.
	 * 
	 * @param request
	 * @return ResourceResults containing ResourceResult objects matching the
	 *         WMRequest
	 * 
	 * @throws ResourceIndexNotAvailableException
	 * @throws ResourceNotInArchiveException
	 * @throws BadQueryException 
	 * @throws AccessControlException 
	 */
	public SearchResults query(final WaybackRequest request)
			throws ResourceIndexNotAvailableException,
			ResourceNotInArchiveException, BadQueryException,
			AccessControlException;

	public void shutdown() throws IOException;
}
