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

import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.bdb.BDBRecord;
import org.archive.wayback.bdb.BDBRecordSet;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;

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
