/* ResourceFileLocationDB
 *
 * $Id$
 *
 * Created on 2:01:29 PM Jun 5, 2008.
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
package org.archive.wayback.resourcestore.locationdb;

import java.io.IOException;

import org.archive.wayback.util.CloseableIterator;

/**
 * Interface to a database that maps file key Strings to zero or more value 
 * Strings. Additionally, the database supports a "getCurrentMark" call that 
 * will return an long value. The results of two independent calls to 
 * getCurrentMark() can be passed to getNamesBetweenMarks() to retrieve an
 * Iterator listing all key Strings added to the database between the two calls
 * to getCurrentMark()  
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface ResourceFileLocationDB {

	public void shutdown() throws IOException;

	public String[] nameToUrls(final String name) 
		throws IOException;

	public void addNameUrl(final String name, final String url) 
		throws IOException;

	public void removeNameUrl(final String name, final String url) 
		throws IOException;

	public CloseableIterator<String> getNamesBetweenMarks(long start, long end)
		throws IOException;

	public long getCurrentMark() throws IOException;
}
