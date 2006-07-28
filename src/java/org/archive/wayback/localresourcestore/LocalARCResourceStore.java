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
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceNotAvailableException;

/**
 * Implements ResourceStore using a local directory of ARC files.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class LocalARCResourceStore implements ResourceStore {
	private static final String RESOURCE_PATH = "resourcestore.arcpath";

	private String path = null;

	/**
	 * Constructor
	 */
	public LocalARCResourceStore() {
		super();
	}

	public void init(Properties p) throws ConfigurationException {
		String configPath = (String) p.get(RESOURCE_PATH);
		if ((configPath == null) || (configPath.length() < 1)) {
			throw new ConfigurationException("Failed to find " + RESOURCE_PATH);
		}
		path = configPath;

	}

	public Resource retrieveResource(SearchResult result) throws IOException, 
		ResourceNotAvailableException {

		ARCLocation location = resultToARCLocation(result);
		String arcName = location.getName();
		if (!arcName.endsWith(ARCReader.DOT_COMPRESSED_ARC_FILE_EXTENSION)) {
			arcName += ARCReader.DOT_COMPRESSED_ARC_FILE_EXTENSION;
		}
		File arcFile = new File(arcName);
		if (!arcFile.isAbsolute()) {
			arcFile = new File(this.path, arcName);
		}
		if (!arcFile.exists() || !arcFile.canRead()) {
			
			// TODO: this needs to be prettied up for end user consumption..
			throw new ResourceNotAvailableException("Cannot find ARC file ("
					+ arcFile.getAbsolutePath() + ")");
		} else {

			// TODO: does this "just work" with HTTP 1.1 ranges?
			// seems like we'd have to know the length for that to work..
			ARCReader reader = ARCReaderFactory.get(arcFile);

			Resource r = new Resource(reader.get(location.getOffset()), reader);
			return r;
		}
	}

	/**
	 * @param result
	 * @return ARCLocation (filename + offset) for searchResult
	 */
	protected ARCLocation resultToARCLocation(SearchResult result) {
		final String daArcName = result.get(WaybackConstants.RESULT_ARC_FILE);
		final long daOffset = Long.parseLong(result
				.get(WaybackConstants.RESULT_OFFSET));

		return new ARCLocation() {
			private String filename = daArcName;

			private long offset = daOffset;

			public String getName() {
				return this.filename;
			}

			public long getOffset() {
				return this.offset;
			}
		};
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
}
