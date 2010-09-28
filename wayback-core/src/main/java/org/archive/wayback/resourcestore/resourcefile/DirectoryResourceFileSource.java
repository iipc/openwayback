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
package org.archive.wayback.resourcestore.resourcefile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Local directory tree holding ARC and WARC files.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DirectoryResourceFileSource implements ResourceFileSource {
	private static final Logger LOGGER =
        Logger.getLogger(DirectoryResourceFileSource.class.getName());

	private static char SEPRTR = '_';
	private String name = null;
	private String path = null;
	private File root = null;
	private FilenameFilter filter = new ArcWarcFilenameFilter();
	private boolean recurse = true;

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.resourcefile.ResourceFileSource#getFileList()
	 */
	public ResourceFileList getResourceFileList() throws IOException {
		if(root == null) {
			throw new IOException("No prefix set");
		}
		ResourceFileList list = new ResourceFileList();
		populateFileList(list,root,recurse);
		return list;
	}

	/**
	 * add all files matching this.filter beneath root to list, recursing if
	 * recurse is set.
	 * 
	 * @param list
	 * @param root
	 * @param recurse
	 * @throws IOException
	 */
	private void populateFileList(ResourceFileList list, File root, boolean recurse) 
	throws IOException {
		if(root.isDirectory()) {
			File[] files = root.listFiles();
			if(files != null) {
				for(File file : files) {
					if(file.isFile() && filter.accept(root, file.getName())) {
						ResourceFileLocation location = new ResourceFileLocation(
								file.getName(),file.getAbsolutePath());
						list.add(location);
					} else if(recurse && file.isDirectory()){
						populateFileList(list, file, recurse);
					}
				}
			}
		} else {
			LOGGER.warning(root.getAbsolutePath() +	" is not a directory.");
			return;
		}
	}
	
	public String getBasename(String path) {
		int sepIdx = path.lastIndexOf(File.separatorChar);
		if(sepIdx != -1) {
			return path.substring(sepIdx + 1);
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.resourcefile.ResourceFileSource#getName()
	 */
	public String getName() {
		if(name != null) {
			return name;
		}
		if(root != null) {
			return root.getAbsolutePath().replace(File.separatorChar, SEPRTR);
		}
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.resourcefile.ResourceFileSource#getPrefix()
	 */
	public String getPrefix() {
		return path;
	}
	public void setPrefix(String path) {
		this.path = path;
		root = new File(path);
	}

	public boolean isRecurse() {
		return recurse;
	}

	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	public FilenameFilter getFilter() {
		return filter;
	}

	public void setFilter(FilenameFilter filter) {
		this.filter = filter;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.resourcefile.ResourceFileSource#getSources()
	 */
	public List<ResourceFileSource> getSources() {
		List<ResourceFileSource> sources = new ArrayList<ResourceFileSource>();
		sources.add(this);
		return sources;
	}
}
