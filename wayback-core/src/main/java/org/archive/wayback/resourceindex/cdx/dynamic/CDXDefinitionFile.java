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
package org.archive.wayback.resourceindex.cdx.dynamic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.archive.wayback.util.flatfile.FlatFile;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CDXDefinitionFile {

	private FlatFile cachedFile;
	
	/**
	 * @param cachedFile
	 */
	public CDXDefinitionFile(FlatFile cachedFile) {
		this.cachedFile = cachedFile;
	}
	
	/**
	 * @param rangeName
	 * @return String[] of MD5s assigned to rangeName
	 * @throws IOException
	 */
	public Object[] getMD5sForRange(String rangeName) throws IOException {
		HashMap<String,Object> matches = new HashMap<String, Object>();
		Iterator<String> itr = cachedFile.getSequentialIterator();
		while(itr.hasNext()) {
			String line = itr.next();
			if(line.startsWith(rangeName)) {
				String md5s[] = line.substring(rangeName.length()+1).split(" ");
				for(int i = 0; i<md5s.length; i++) {
					if(!matches.containsKey(md5s[i])) {
						matches.put(md5s[i],null);
					}
				}
			}
		}
		return matches.keySet().toArray();
	}
}