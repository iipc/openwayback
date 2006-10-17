/* BDBIndex
 *
 * $Id$
 *
 * Created on 4:48:46 PM Aug 17, 2006.
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
package org.archive.wayback.resourceindex.bdb;

import java.io.IOException;
import java.util.Iterator;

import org.archive.wayback.bdb.BDBRecordSet;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.SearchResultSource;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.CloseableIterator;

import com.sleepycat.je.DatabaseException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBIndex extends BDBRecordSet implements SearchResultSource {

	private CloseableIterator adaptIterator(Iterator itr) {
		return new AdaptedIterator(itr,new BDBRecordToSearchResultAdapter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator getPrefixIterator(String prefix)
			throws ResourceIndexNotAvailableException {
		
		try {
			return adaptIterator(recordIterator(prefix,true));
		} catch (DatabaseException e) {
			throw new ResourceIndexNotAvailableException(e.getMessage()); 
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixReverseIterator(java.lang.String)
	 */
	public CloseableIterator getPrefixReverseIterator(String prefix)
			throws ResourceIndexNotAvailableException {
		try {
			return adaptIterator(recordIterator(prefix,false));
		} catch (DatabaseException e) {
			throw new ResourceIndexNotAvailableException(e.getMessage()); 
		}
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#cleanup(org.archive.wayback.util.CleanableIterator)
	 */
	public void cleanup(CloseableIterator c) throws IOException {
		c.close();
	}

}
