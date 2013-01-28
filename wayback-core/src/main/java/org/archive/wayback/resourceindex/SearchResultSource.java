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
package org.archive.wayback.resourceindex;

import java.io.IOException;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.util.iterator.CloseableIterator;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public interface SearchResultSource {
	/**
	 * @param prefix
	 * @return CleanableIterator that will return SearchResults beginning with prefix
	 *         argument, with subsequent next() calls returning subsequent
	 *         results.
	 * @throws ResourceIndexNotAvailableException 
	 */
	public CloseableIterator<CaptureSearchResult> getPrefixIterator(final String prefix)
			throws ResourceIndexNotAvailableException;
	
	/**
	 * @param prefix
	 * @return CleanableIterator that will return SearchResults starting *before* prefix
	 *         argument, with subsequent next() calls returning previous
	 *         results.
	 * @throws ResourceIndexNotAvailableException 
	 */
	public CloseableIterator<CaptureSearchResult> getPrefixReverseIterator(final String prefix)
			throws ResourceIndexNotAvailableException;
	
	/**
	 * @param c
	 * @throws IOException 
	 */
	public void cleanup(CloseableIterator<CaptureSearchResult> c) throws IOException;

	/**
	 * @param c
	 * @throws IOException 
	 */
	public void shutdown() throws IOException;
}
