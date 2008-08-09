/* LocalResourceFileResourceStore
 *
 * $Id$
 *
 * Created on 6:17:54 PM May 29, 2008.
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
package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDB;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;

/**
 * Simple ResourceStore implementation, which uses a ResourceFileLocationDB to
 * locate ARC/WARC files, that can be remote(via http://) or local paths.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LocationDBResourceStore implements ResourceStore {

	private ResourceFileLocationDB db = null;

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceStore#retrieveResource(org.archive.wayback.core.SearchResult)
	 */
	public Resource retrieveResource(CaptureSearchResult result) throws IOException,
			ResourceNotAvailableException {
		// extract ARC filename
		String fileName = result.getFile();
		if(fileName == null || fileName.length() < 1) {
			throw new IOException("No ARC/WARC name in search result...");
		}

		String urls[] = db.nameToUrls(fileName);
		if(urls == null || urls.length == 0) {
			throw new ResourceNotAvailableException("Unable to locate(" +
					fileName + ")");
		}
		
		final long offset = result.getOffset();

		Resource r = null;
		// TODO: attempt multiple threads?
		for(String url : urls) {
				
			try {

				if(url.startsWith("http://")) {
					r = ResourceFactory.getResource(new URL(url), offset);
				} else {
					// assume local path:
					r = ResourceFactory.getResource(new File(url), offset);
				}
				// TODO: attempt to grab the first few KB? The underlying 
				// 		InputStreams support mark(), so we could reset() after.
				//      wait for now, currently this will parse HTTP headers, 
				//      which means we've already read some
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(r != null) {
				break;
			}
		}
		if(r == null) {
			throw new ResourceNotAvailableException("Unable to retrieve");
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceStore#shutdown()
	 */
	public void shutdown() throws IOException {
		// NOOP
	}

	public ResourceFileLocationDB getDb() {
		return db;
	}

	public void setDb(ResourceFileLocationDB db) {
		this.db = db;
	}
}
