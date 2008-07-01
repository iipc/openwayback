/* CaptureToUrlSearchResultAdapter
 *
 * $Id$
 *
 * Created on 4:45:55 PM Jun 28, 2008.
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
package org.archive.wayback.core;

import java.util.HashMap;

import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.CloseableIterator;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CaptureToUrlSearchResultAdapter
	implements Adapter<CaptureSearchResult, UrlSearchResult> {

	private String currentUrl;
	private String originalUrl;
	private String firstCapture;
	private String lastCapture;
	private int numCaptures;
	private HashMap<String,Object> digests;
	private UrlSearchResult resultRef = null;
	public CaptureToUrlSearchResultAdapter() {
		
	}
	private UrlSearchResult makeUrlSearchResult(CaptureSearchResult result) {
		currentUrl = result.getUrlKey();
		originalUrl = result.getOriginalUrl();
		firstCapture = result.getCaptureTimestamp();
		lastCapture = firstCapture;
		digests = new HashMap<String,Object>();
		digests.put(result.getDigest(),null);
		numCaptures = 1;

		resultRef = new UrlSearchResult();
		resultRef.setUrlKey(currentUrl);
		resultRef.setOriginalUrl(originalUrl);
		resultRef.setFirstCapture(firstCapture);
		resultRef.setLastCapture(lastCapture);
		resultRef.setNumCaptures(1);
		resultRef.setNumVersions(1);
		return resultRef;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public UrlSearchResult adapt(CaptureSearchResult c) {
		String urlKey = c.getUrlKey();
		if(resultRef == null || !currentUrl.equals(urlKey)) {
			return makeUrlSearchResult(c);
		}

		// same url -- accumulate into the last one we returned:
		String captureDate = c.getCaptureTimestamp();
		if(captureDate.compareTo(firstCapture) < 0) {
			firstCapture = captureDate;
			resultRef.setFirstCapture(firstCapture);
		}
		if(captureDate.compareTo(lastCapture) > 0) {
			lastCapture = captureDate;
			resultRef.setLastCapture(lastCapture);
		}
		numCaptures++;
		digests.put(c.getDigest(), null);
		resultRef.setNumCaptures(numCaptures);
		resultRef.setNumVersions(digests.size());
		return null;
	}
	public static CloseableIterator<UrlSearchResult> adaptCaptureIterator(
			CloseableIterator<CaptureSearchResult> itr) {

		// HACKHACK: this is pretty lame. We return an UrlSearchResult the
		// first time we see a new urlKey, and cache a reference to the returned
		// UrlSearchResult, updating it as we see subsequent CaptureSearchResult
		// objects with the same urlKey.
		// This means that users of the returned UrlSearchResult need to wait
		// until they've got the *next* returned UrlSearchResult before using
		// the *previous* UrlSearchResult.
		// At the moment, this all happens inside a LocalResourceIndex, so
		// none of the UrlSearchResult objects should be seen/used in any 
		// significant way before they've all be accumulated into an 
		// UrlSearchResults object..
		return new AdaptedIterator<CaptureSearchResult,UrlSearchResult>(itr,
				new CaptureToUrlSearchResultAdapter());
	}
}
