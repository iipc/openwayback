/* LocalARCResourceStore
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.localresourcestore;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.archive.io.arc.ARCLocation;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;

/**
 * Implements ResourceStore using a local directory of ARC files.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class LocalARCResourceStore implements ResourceStore {
	private static final String RESOURCE_PATH = "resourcestore.arcpath";

	private static final String ARCTAIL = ".arc.gz";

	private String path = null;

	/**
	 * Constructor
	 */
	public LocalARCResourceStore() {
		super();
	}

	public void init(Properties p) throws Exception {
		String configPath = (String) p.get(RESOURCE_PATH);
		if ((configPath == null) || (configPath.length() < 1)) {
			throw new IllegalArgumentException("Failed to find "
					+ RESOURCE_PATH);
		}
		path = configPath;

	}

	public Resource retrieveResource(ARCLocation location) throws IOException {
		String arcName = location.getName();
		if (!arcName.endsWith(ARCTAIL)) {
			arcName += ARCTAIL;
		}
		File arcFile = new File(arcName);
		if (!arcFile.isAbsolute()) {
			arcFile = new File(this.path, arcName);
		}
		if (!arcFile.exists() || !arcFile.canRead()) {
			throw new IOException("Cannot find ARC file ("
					+ arcFile.getAbsolutePath() + ")");
		} else {
			ARCReader reader = ARCReaderFactory.get(arcFile);
			Resource r = new Resource(reader.get(location.getOffset()));
			return r;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
}
