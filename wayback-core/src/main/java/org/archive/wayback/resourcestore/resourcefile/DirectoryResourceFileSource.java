/* DirectoryResourceFileSource
 *
 * $Id$
 *
 * Created on 4:00:49 PM May 29, 2008.
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
package org.archive.wayback.resourcestore.resourcefile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Local directory tree holding ARC and WARC files.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DirectoryResourceFileSource implements ResourceFileSource {

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
		
		File[] files = root.listFiles();
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
