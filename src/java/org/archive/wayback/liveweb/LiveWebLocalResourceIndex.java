/* LiveWebLocalResourceIndex
 *
 * $Id$
 *
 * Created on 5:53:29 PM Mar 13, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.liveweb;

import java.util.ArrayList;
import java.util.Properties;

import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.SearchResultSourceFactory;
import org.archive.wayback.resourceindex.bdb.BDBIndex;
import org.archive.wayback.resourceindex.indexer.SearchResultToBDBRecordAdapter;
import org.archive.wayback.util.AdaptedIterator;

/**
 * Alternate LocalResourceIndex that supports an alternate BDB configuration,
 * and allows adding of SearchResults to the index.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LiveWebLocalResourceIndex extends LocalResourceIndex {
	private final static String LW_DB_DIR = "liveweb.dbdir";
	private final static String LW_DB_NAME = "liveweb.dbname";
	
	private String getProp(Properties p, String key) 
		throws ConfigurationException {
		
		if(p.containsKey(key)) {
			String v = p.getProperty(key);
			if(v == null || v.length() < 1) {
				throw new ConfigurationException("Empty configuration " + key);				
			}
			return v;
		} else {
			throw new ConfigurationException("Missing configuration " + key);
		}
		
	}

	public void init(Properties p) throws ConfigurationException {
		// use alternate Properties, to differentiate config from normal 
		// ResourceIndex
		Properties newP = new Properties();

		newP.setProperty(SearchResultSourceFactory.SOURCE_CLASS, 
				SearchResultSourceFactory.SOURCE_CLASS_BDB);

		newP.setProperty(SearchResultSourceFactory.INDEX_PATH,
				getProp(p,LW_DB_DIR));

		newP.setProperty(SearchResultSourceFactory.DB_NAME,
				getProp(p,LW_DB_NAME));

		super.init(newP);
	}
	
	/**
	 * Add a single SearchResult to the index.
	 * @param result
	 */
	public void addSearchResult(SearchResult result) {
		ArrayList l = new ArrayList();
		l.add(result);
		BDBIndex bdbSource = (BDBIndex) source;
		bdbSource.insertRecords(new AdaptedIterator(l.iterator(),
				new SearchResultToBDBRecordAdapter()));
	}
}
