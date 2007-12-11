/* HttpARCResourceStore
 *
 * $Id$
 *
 * Created on 5:29:56 PM Oct 12, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore;

import java.io.IOException;
import java.net.URL;

import org.archive.wayback.ResourceStore;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ResourceNotAvailableException;


/**
 * Implements ResourceStore where ARCs are accessed via HTTP 1.1 range requests.
 * All ARC files are assumed to be "rooted" at a particular HTTP URL, within
 * a single directory, implying an ARC file reverse-proxy to connect through
 * to actual HTTP ARC locations.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class Http11ResourceStore implements ResourceStore {

	private String urlPrefix = null;


	public Resource retrieveResource(SearchResult result) throws IOException, 
		ResourceNotAvailableException {

		// extract ARC filename + add .arc.gz if it is not present
		String arcName = result.get(WaybackConstants.RESULT_ARC_FILE);
		if(arcName == null || arcName.length() < 1) {
			throw new IOException("No ARC/WARC name in search result...");
		}

		// extract ARC offset + convert to long
		final String offsetString = result.get(WaybackConstants.RESULT_OFFSET);
		if(offsetString == null || offsetString.length() < 1) {
			throw new IOException("No ARC/WARC offset in search result...");
		}
		final long offset = Long.parseLong(offsetString);

		String arcUrl = urlPrefix + arcName;
		Resource r = null;
		try {

			r = ResourceFactory.getResource(new URL(arcUrl), offset);

		} catch (IOException e) {

			e.printStackTrace();
			throw new ResourceNotAvailableException("Unable to retrieve",
					e.getLocalizedMessage());
		}
		return r;
	}

	/**
	 * @return the urlPrefix
	 */
	public String getUrlPrefix() {
		return urlPrefix;
	}

	/**
	 * @param urlPrefix the urlPrefix to set
	 */
	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}
}
