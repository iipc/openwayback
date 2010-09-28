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

import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.bdb.BDBRecord;
import org.archive.wayback.util.bdb.BDBRecordSet;

import com.sleepycat.je.DatabaseEntry;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResultToBDBRecordAdapter implements 
	Adapter<CaptureSearchResult,BDBRecord> {
	private static final Logger LOGGER =
        Logger.getLogger(SearchResultToBDBRecordAdapter.class.getName());
	
	DatabaseEntry key = new DatabaseEntry();

	DatabaseEntry value = new DatabaseEntry();

	BDBRecord record = new BDBRecord(key, value);

	private UrlCanonicalizer canonicalizer = null;

	private final static String DELIMITER = " ";
	
	public SearchResultToBDBRecordAdapter(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public BDBRecord adapt(CaptureSearchResult result) {
		StringBuilder keySB = new StringBuilder(40);
		StringBuilder valSB = new StringBuilder(100);
		
		String origUrl = result.getOriginalUrl();
		String urlKey;
		try {
			urlKey = canonicalizer.urlStringToKey(origUrl);
		} catch (URIException e) {
//			e.printStackTrace();
			LOGGER.warning("FAILED canonicalize(" + origUrl +")");
			urlKey = origUrl;
		}
		keySB.append(urlKey);
		keySB.append(DELIMITER);
		keySB.append(result.getCaptureTimestamp());
		keySB.append(DELIMITER);
		keySB.append(result.getOffset());
		keySB.append(DELIMITER);
		keySB.append(result.getFile());
		

		valSB.append(result.getOriginalUrl());
		valSB.append(DELIMITER);
		valSB.append(result.getMimeType());
		valSB.append(DELIMITER);
		valSB.append(result.getHttpCode());
		valSB.append(DELIMITER);
		valSB.append(result.getDigest());
		valSB.append(DELIMITER);
		valSB.append(result.getRedirectUrl());

		key.setData(BDBRecordSet.stringToBytes(keySB.toString()));
		value.setData(BDBRecordSet.stringToBytes(valSB.toString()));

		return record;
	}
}
