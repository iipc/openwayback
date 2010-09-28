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
package org.archive.wayback.util;

import java.io.File;
import java.io.IOException;

/**
 * Lots of things need to transform Strings to Files, constructing them if 
 * needed. These are static methods for doing that.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DirMaker {
	/**
	 * Ensure that the path pointed to by 'path' is a directory, if possible
	 * @param path
	 * @param name
	 * @return File that is a created directory
	 * @throws IOException
	 */
	public static File ensureDir(String path, String name) throws IOException {
		if((path == null) || (path.length() == 0)) {
			throw new IOException("No configuration for (" + name + ")");
		}
		File dir = new File(path);
		if(dir.exists()) {
			if(!dir.isDirectory()) {
				throw new IOException("Dir(" + name + ") at (" + path + 
						") exists but is not a directory!");
			}
		} else {
			if(!dir.mkdirs()) {
				throw new IOException("Unable to create dir(" + name +") at ("
						+ path + ")");
			}
		}
		return dir;
	}
	public static File ensureDir(String path) throws IOException {
		return ensureDir(path,"");
	}
	public static String getAbsolutePath(File file) {
		if(file == null) {
			return null;
		}
		return file.getAbsolutePath();
	}
}
