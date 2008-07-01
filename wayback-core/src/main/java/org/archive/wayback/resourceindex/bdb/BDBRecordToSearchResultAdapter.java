/* BDBRecordToSearchResultAdapter
 *
 * $Id$
 *
 * Created on 4:52:17 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.bdb;

import java.io.UnsupportedEncodingException;

import org.archive.wayback.bdb.BDBRecord;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.CDXLineToSearchResultAdapter;
import org.archive.wayback.util.Adapter;

/**
 * Adapter that converts a BDBRecord into a SearchResult
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBRecordToSearchResultAdapter 
	implements Adapter<BDBRecord,CaptureSearchResult> {

	private static int DEFAULT_SB_SIZE = 100;
	private StringBuilder sb;
	/**
	 * Constructor
	 */
	public BDBRecordToSearchResultAdapter() {
		sb = new StringBuilder(DEFAULT_SB_SIZE);
	}
	
	/**
	 * @param record
	 * @return SearchResult representation of input BDBRecord
	 */
	public CaptureSearchResult adapt(BDBRecord record) {
		sb.setLength(0);
		try {
			String key = new String(record.getKey().getData(),"UTF-8");
			int urlEnd = key.indexOf(' ');
			int dateSpecEnd = key.indexOf(' ',urlEnd + 1);
			sb.append(key.substring(0,dateSpecEnd));
			sb.append(" ");
			sb.append(new String(record.getValue().getData(),"UTF-8"));
			sb.append(key.substring(dateSpecEnd));
		} catch (UnsupportedEncodingException e) {
			// should not happen with UTF-8 hard-coded..
			e.printStackTrace();
		}
		return CDXLineToSearchResultAdapter.doAdapt(sb.toString());
	}
}
