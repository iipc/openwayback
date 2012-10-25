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
package org.archive.wayback.resourceindex.bdb;

import java.io.UnsupportedEncodingException;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.CDXLineToSearchResultAdapter;
import org.archive.wayback.resourceindex.cdx.format.CDXFlexFormat;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.ByteOp;
import org.archive.wayback.util.bdb.BDBRecord;

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
		String key = new String(record.getKey().getData(),ByteOp.UTF8);
		int urlEnd = key.indexOf(' ');
		int dateSpecEnd = key.indexOf(' ',urlEnd + 1);
		sb.append(key.substring(0,dateSpecEnd));
		sb.append(" ");
		sb.append(new String(record.getValue().getData(),ByteOp.UTF8));
		sb.append(key.substring(dateSpecEnd));
		return CDXFlexFormat.parseCDXLineFlex(sb.toString());
	}
}
