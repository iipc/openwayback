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

import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ArcWarcFilenameFilter;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;


/**
 * Implements ResourceStore where ARC/WARCs are accessed via a local file or an
 * HTTP 1.1 range request. All files are assumed to be "rooted" at a particular
 * HTTP URL, or within a single local directory. The HTTP version may imply a
 * file reverse-proxy to connect through to actual HTTP ARC/WARC locations.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SimpleResourceStore implements ResourceStore {

	private String prefix = null;

	public Resource retrieveResource(CaptureSearchResult result)
		throws ResourceNotAvailableException {

		// extract ARC filename
		String fileName = result.getFile();
		if(fileName == null || fileName.length() < 1) {
			throw new ResourceNotAvailableException("No ARC/WARC name in search result...");
		}

		final long offset = result.getOffset();
		if(!fileName.endsWith(ArcWarcFilenameFilter.ARC_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.ARC_GZ_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.WARC_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.WARC_GZ_SUFFIX)) {
			fileName = fileName + ArcWarcFilenameFilter.ARC_GZ_SUFFIX;
		}
				
		String fileUrl = prefix + fileName;
		Resource r = null;
		try {

			r = ResourceFactory.getResource(fileUrl, offset);

		} catch (IOException e) {

			e.printStackTrace();
			throw new ResourceNotAvailableException("Unable to retrieve",
					e.getLocalizedMessage());
		}
		return r;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void shutdown() throws IOException {
		// no-op
	}
}
