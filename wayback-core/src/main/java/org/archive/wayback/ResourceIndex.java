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

import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;

/**
 * Transforms a WaybackRequest into a ResourceResults.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public interface ResourceIndex {
	/**
	 * Transform a WaybackRequest into a ResourceResults.
	 * 
	 * @param request WaybackRequest object from RequestParser
	 * @return SearchResults containing SearchResult objects matching the
	 *         WaybackRequest
	 * 
	 * @throws ResourceIndexNotAvailableException if the ResourceIndex
	 * 			is not available (remote host down, local files missing, etc)
	 * @throws ResourceNotInArchiveException if the ResourceIndex could be
	 * 			contacted, but no SearchResult objects matched the request
	 * @throws BadQueryException if the WaybackRequest is lacking information
	 * 			required to make a reasonable search of this ResourceIndex
	 * @throws AccessControlException if SearchResult objects actually matched,
	 * 			but could not be returned due to AccessControl restrictions
	 * 			(robots.txt documents, Administrative URL blocks, etc)
	 */
	public SearchResults query(final WaybackRequest request)
			throws ResourceIndexNotAvailableException,
			ResourceNotInArchiveException, BadQueryException,
			AccessControlException;

	/**
	 * Release any resources used by this ResourceIndex cleanly
	 * @throws IOException for usual causes
	 */
	public void shutdown() throws IOException;
}
