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
import java.util.logging.Logger;

import org.archive.wayback.util.CloseableIterator;

/**
 * Implementation of ResourceFileLocationDB that prepends a prefix to
 * the name.
 */
public class PrefixResourceFileLocationDB implements ResourceFileLocationDB  {
	private final static Logger LOGGER =
		Logger.getLogger(PrefixResourceFileLocationDB.class.getName());
	private String prefix = null;

	public void addNameUrl(String name, String url) throws IOException {
		// NO-OP
	}

	public long getCurrentMark() throws IOException {
		return 0;
	}

	public CloseableIterator<String> getNamesBetweenMarks(long start, long end)
			throws IOException {
		return null;
	}

	@SuppressWarnings("unchecked")
	public String[] nameToUrls(String name) throws IOException {
		String[] values = new String[1];
                values[0] = prefix + name;
                return values;
	}

	public void removeNameUrl(String name, String url) throws IOException {
		// NO-OP
	}

	public void shutdown() throws IOException {
		// NO-OP
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}
}