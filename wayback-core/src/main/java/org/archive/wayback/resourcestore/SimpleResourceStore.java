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

	private final static Logger LOGGER = Logger.getLogger(
			SimpleResourceStore.class.getName());
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
			LOGGER.warning("Unable to retrieve:" + fileUrl + ":" + offset);
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
