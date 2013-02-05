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
package org.archive.wayback.resourceindex.adapters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.UrlSearchResult;
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
