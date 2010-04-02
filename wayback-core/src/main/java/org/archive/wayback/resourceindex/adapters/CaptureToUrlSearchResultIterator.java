/* CaptureToUrlSearchResultIterator
 *
 * $Id$:
 *
 * Created on Mar 31, 2010.
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

package org.archive.wayback.resourceindex.adapters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.UrlSearchResult;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.PeekableIterator;

/**
 * @author brad
 *
 */
public class CaptureToUrlSearchResultIterator implements CloseableIterator<UrlSearchResult> {
	private static final Logger LOGGER = Logger.getLogger(
			CaptureToUrlSearchResultIterator.class.getName());
	private PeekableIterator<CaptureSearchResult> peek = null;
	UrlSearchResult cachedNext = null;
	/**
	 * @param itr possibly closeable iterator of CaptureSearchResult objects
	 */
	public CaptureToUrlSearchResultIterator(Iterator<CaptureSearchResult> itr) {
		peek = new PeekableIterator<CaptureSearchResult>(itr);
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		createNext();
		return (cachedNext != null);
	}

	private void createNext() {
		if(cachedNext == null) {
			if(peek.hasNext()) {
				// populate 
				CaptureSearchResult captureResult = peek.next();
				String currentKey = captureResult.getUrlKey();
				String originalUrl = captureResult.getOriginalUrl();
				String firstCapture = captureResult.getCaptureTimestamp();
				LOGGER.info("Creating new UrlResult:" + currentKey + " " + 
						firstCapture);
				String lastCapture = firstCapture;
				HashMap<String,Object> digests = new HashMap<String,Object>();
				digests.put(captureResult.getDigest(),null);
				int numCaptures = 1;

				cachedNext = new UrlSearchResult();
				cachedNext.setUrlKey(currentKey);
				cachedNext.setOriginalUrl(originalUrl);

				// now rip through the rest until we find either the last
				// in the iterator, or the first having a different urlKey:
				while((captureResult = peek.peekNext()) != null) {
					String urlKey = captureResult.getUrlKey();
					if(currentKey.equals(urlKey)) {
						// remove from iterator, and accumulate:
						peek.next();
						numCaptures++;
						digests.put(captureResult.getDigest(), null);

						String captureTS = captureResult.getCaptureTimestamp();
						if(captureTS.compareTo(firstCapture) < 0) {
							firstCapture = captureTS;
						}
						if(captureTS.compareTo(lastCapture) > 0) {
							lastCapture = captureTS;
						}

					} else {
						// all done. leave the next result and stop processing:
						LOGGER.info("Hit next urlKey. Cur("+currentKey+") new("
								+ urlKey + ")");
						break;
					}
				}
				cachedNext.setFirstCapture(firstCapture);
				cachedNext.setLastCapture(lastCapture);
				cachedNext.setNumCaptures(numCaptures);
				cachedNext.setNumVersions(digests.size());
			}
		}
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public UrlSearchResult next() {
		if(cachedNext == null) {
			throw new NoSuchElementException("use hasNext!");
		}
		UrlSearchResult tmp = cachedNext;
		cachedNext = null;
		return tmp;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		peek.close();
	}
}
