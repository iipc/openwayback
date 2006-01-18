/* Http11ResourceStore
 *
 * $Id$
 *
 * Created on 4:14:02 PM Dec 16, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
package org.archive.wayback.http11resourcestore;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceNotAvailableException;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class Http11ResourceStore implements ResourceStore {
	
	private FileLocationDB locationDB = new FileLocationDB();

	/**
	 * Constructor
	 */
	public Http11ResourceStore() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {

		locationDB.init(p);

	}

	public Resource retrieveResource(SearchResult result) throws IOException, 
		ResourceNotAvailableException {

		// extract ARC filename + add .arc.gz if it is not present
		String arcName = result.get(WaybackConstants.RESULT_ARC_FILE);
		if(arcName == null || arcName.length() < 1) {
			throw new IOException("No ARC name in search result...");
		}
		if (!arcName.endsWith(ARCReader.DOT_COMPRESSED_ARC_FILE_EXTENSION)) {
			arcName += ARCReader.DOT_COMPRESSED_ARC_FILE_EXTENSION;
		}

		// extract ARC offset + convert to long
		final String offsetString = result.get(WaybackConstants.RESULT_OFFSET);
		if(offsetString == null || offsetString.length() < 1) {
			throw new IOException("No ARC offset in search result...");
		}
		final long offset = Long.parseLong(offsetString);

		// lookup URLs for arcName
		String[] arcUrls;
		try {
			arcUrls = locationDB.arcToUrls(arcName);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResourceNotAvailableException("Arc location DB error " +
					e.getMessage());
		}
		if(arcUrls == null || arcUrls.length < 1) {
			
			// TODO: pretty up message for end user consumption
			throw new ResourceNotAvailableException("Unable to location ARC " +
					arcName);
		}
		
		// for now, we'll just grab the first one:
		String arcUrl = arcUrls[0];
		ARCReader ar = ARCReaderFactory.get(new URL(arcUrl),offset);
		ARCRecord rec = ar.get();
		Resource r = new Resource(rec,ar);

		return r;
	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
}
