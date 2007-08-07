/* SearchResultToBDBRecordAdapter
 *
 * $Id$
 *
 * Created on 5:58:22 PM Mar 13, 2007.
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
package org.archive.wayback.resourceindex.bdb;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.bdb.BDBRecord;
import org.archive.wayback.bdb.BDBRecordSet;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.Adapter;

import com.sleepycat.je.DatabaseEntry;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResultToBDBRecordAdapter implements 
	Adapter<SearchResult,BDBRecord> {

	DatabaseEntry key = new DatabaseEntry();

	DatabaseEntry value = new DatabaseEntry();

	BDBRecord record = new BDBRecord(key, value);

	private final static String DELIMITER = " ";
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public BDBRecord adapt(SearchResult result) {
		StringBuilder keySB = new StringBuilder(40);
		StringBuilder valSB = new StringBuilder(100);
		
		
		keySB.append(result.get(WaybackConstants.RESULT_URL_KEY));
		keySB.append(DELIMITER);
		keySB.append(result.get(WaybackConstants.RESULT_CAPTURE_DATE));
		keySB.append(DELIMITER);
		keySB.append(result.get(WaybackConstants.RESULT_OFFSET));
		keySB.append(DELIMITER);
		keySB.append(result.get(WaybackConstants.RESULT_ARC_FILE));
		

		valSB.append(result.get(WaybackConstants.RESULT_ORIG_HOST));
		valSB.append(DELIMITER);
		valSB.append(result.get(WaybackConstants.RESULT_MIME_TYPE));
		valSB.append(DELIMITER);
		valSB.append(result.get(WaybackConstants.RESULT_HTTP_CODE));
		valSB.append(DELIMITER);
		valSB.append(result.get(WaybackConstants.RESULT_MD5_DIGEST));
		valSB.append(DELIMITER);
		valSB.append(result.get(WaybackConstants.RESULT_REDIRECT_URL));

		key.setData(BDBRecordSet.stringToBytes(keySB.toString()));
		value.setData(BDBRecordSet.stringToBytes(valSB.toString()));

		return record;
	}
}
