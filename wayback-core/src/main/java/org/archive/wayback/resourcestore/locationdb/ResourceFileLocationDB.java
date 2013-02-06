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
package org.archive.wayback.resourcestore.locationdb;

import java.io.IOException;

import org.archive.util.iterator.CloseableIterator;



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
