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
import java.util.Properties;

import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.exception.WaybackException;

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
	 * @throws IOException
	 * @throws WaybackException
	 */
	public ResourceResults query(final WMRequest request) throws IOException,
			WaybackException;

	/**
	 * Initialize this ResourceIndex. Pass in the specific configurations via
	 * Properties.
	 * 
	 * @param p
	 *            Generic properties bag for configurations
	 * @throws Exception
	 */
	public void init(Properties p) throws Exception;
}
