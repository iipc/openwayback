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
package org.archive.wayback.resourcestore;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
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
	private static final Logger LOGGER =
        Logger.getLogger(LocationDBResourceStore.class.getName());

	private ResourceFileLocationDB db = null;

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceStore#retrieveResource(org.archive.wayback.core.SearchResult)
	 */
	public Resource retrieveResource(CaptureSearchResult result) 
		throws ResourceNotAvailableException {
		// extract ARC filename
		String fileName = result.getFile();
		if(fileName == null || fileName.length() < 1) {
			throw new ResourceNotAvailableException("No ARC/WARC name in search result...", fileName);
		}

		String urls[];
		try {
			urls = db.nameToUrls(fileName);
		} catch (IOException e1) {
			//e1.printStackTrace();
			throw new ResourceNotAvailableException(e1.getLocalizedMessage(), fileName, HttpServletResponse.SC_NOT_FOUND);
		}
		if(urls == null || urls.length == 0) {
			String msg = "Unable to locate(" + fileName + ")";
			LOGGER.info(msg);
			throw new ResourceNotAvailableException(msg, fileName, HttpServletResponse.SC_NOT_FOUND);
		}
		
		final long offset = result.getOffset();

		String errMsg = "Unable to retrieve";
		Exception origException = null;
		
		Resource r = null;
		// TODO: attempt multiple threads?
		for(String url : urls) {
				
			try {

				r = ResourceFactory.getResource(url, offset);
				// TODO: attempt to grab the first few KB? The underlying 
				// 		InputStreams support mark(), so we could reset() after.
				//      wait for now, currently this will parse HTTP headers, 
				//      which means we've already read some
				
			} catch (IOException e) {
				errMsg = url + " - " + e;
				origException = e;
				LOGGER.info("Unable to retrieve " + errMsg);
			}
			if(r != null) {
				break;
			}
		}
		if(r == null) {
			throw new ResourceNotAvailableException(errMsg, fileName, origException);
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceStore#shutdown()
	 */
	public void shutdown() throws IOException {
		db.shutdown();
	}

	/**
	 * @return the ResourceFileLocationDB used by this ResourceStore
	 */
	public ResourceFileLocationDB getDb() {
		return db;
	}

	/**
	 * @param db the ResourceFileLocationDB to use with this ResourceStore
	 */
	public void setDb(ResourceFileLocationDB db) {
		this.db = db;
	}
}
