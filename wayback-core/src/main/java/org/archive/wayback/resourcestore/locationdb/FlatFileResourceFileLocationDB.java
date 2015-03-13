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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.util.flatfile.FlatFile;

public class FlatFileResourceFileLocationDB implements ResourceFileLocationDB  {
	private final static Logger LOGGER =
		Logger.getLogger(FlatFileResourceFileLocationDB.class.getName());
	private String path = null;
	private String delimiter = "\t";
    protected FlatFile flatFile = null;

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
		ArrayList<String> urls = new ArrayList<String>();
		String prefix = name + delimiter;
		Iterator<String> itr = flatFile.getRecordIterator(prefix);
		while(itr.hasNext()) {
			String line = itr.next();
			if(line.startsWith(prefix)) {
				urls.add(line.substring(prefix.length()));
			} else {
				break;
			}
		}
		
		if(itr instanceof CloseableIterator) {
			CloseableIterator<String> citr = (CloseableIterator<String>) itr;
			citr.close();
		}
		if(urls.size() == 0) {
			LOGGER.info("No locations for " + name + " in " + path);
		}
		String[] a = new String[urls.size()];
		for(int i=0; i < urls.size(); i++) {
			a[i] = urls.get(i);
		}
		return a;
	}

	public void removeNameUrl(String name, String url) throws IOException {
		// NO-OP
	}

	public void shutdown() throws IOException {
		// NO-OP
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
		flatFile = new FlatFile(path);
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param delimter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}
}
