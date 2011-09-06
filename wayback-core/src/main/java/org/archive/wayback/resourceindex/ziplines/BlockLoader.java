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
package org.archive.wayback.resourceindex.ziplines;

import java.io.IOException;

public interface BlockLoader {
	/**
	 * Fetch a range of bytes from a particular URL. Note that the bytes are
	 * read into memory all at once, so care should be taken with the length
	 * argument.
	 * 
	 * @param url String URL to fetch
	 * @param offset byte start offset of the desired range
	 * @param length number of octets to fetch
	 * @return a new byte[] containing the octets fetched
	 * @throws IOException on Network and protocol failures, as well as Timeouts
	 */
	public byte[] getBlock(String url, long offset, int length) 
	throws IOException;
}
