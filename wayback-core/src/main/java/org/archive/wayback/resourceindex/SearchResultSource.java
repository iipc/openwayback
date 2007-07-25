/* SearchResultSource
 *
 * $Id$
 *
 * Created on 4:31:33 PM Aug 17, 2006.
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
package org.archive.wayback.resourceindex;

import java.io.IOException;

import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.util.CloseableIterator;

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
	public CloseableIterator<SearchResult> getPrefixIterator(final String prefix)
			throws ResourceIndexNotAvailableException;

	/**
	 * @param prefix
	 * @return CleanableIterator that will return SearchResults starting *before* prefix
	 *         argument, with subsequent next() calls returning previous
	 *         results.
	 * @throws ResourceIndexNotAvailableException 
	 */
	public CloseableIterator<SearchResult> getPrefixReverseIterator(final String prefix)
			throws ResourceIndexNotAvailableException;
	
	/**
	 * @param c
	 * @throws IOException 
	 */
	public void cleanup(CloseableIterator<SearchResult> c) throws IOException;
}
