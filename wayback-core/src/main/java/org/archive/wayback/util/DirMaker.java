/* DirMaker
 *
 * $Id$
 *
 * Created on 7:02:37 PM Jul 19, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
