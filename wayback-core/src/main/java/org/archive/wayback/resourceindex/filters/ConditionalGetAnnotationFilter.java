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
package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.webapp.AccessPoint;

/**
 * WARC file allows 2 forms of deduplication. The first actually downloads
 * documents and compares their digest with a database of previous values. When
 * a new capture of a document exactly matches the previous digest, an 
 * abbreviated record is stored in the WARC file. The second form uses an HTTP
 * conditional GET request, sending previous values returned for a given URL
 * (etag, last-modified, etc). In this case, the remote server either sends a
 * new document (200) which is stored normally, or the server will return a 
 * 304 (Not Modified) response, which is stored in the WARC file.
 * 
 * For the first record type, the wayback indexer will output a placeholder 
 * record that includes the digest of the last-stored record. For 304 responses,
 * the indexer outputs a normal looking record, but the record will have a
 * SHA1 digest which is easily distinguishable as an "empty" document. The SHA1
 * is always:
 * 
 *   3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ
 *   
 * This class will observe a stream of SearchResults, storing the values for
 * the last seen non-empty SHA1 field. Any subsequent SearchResults with an 
 * empty SHA1 will be annotated, copying the values from the last non-empty 
 * record. 
 * 
 * This is highly experimental.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ConditionalGetAnnotationFilter 
implements ObjectFilter<CaptureSearchResult> {

	private final static String EMPTY_VALUE = "-";
	private final static String EMPTY_SHA1 = "3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ";

	private CaptureSearchResult lastSeen = null;

	private int annotate(CaptureSearchResult o) {
		if(lastSeen == null) {
			// TODO: log missing record digest reference
			return FILTER_EXCLUDE;
		}
		o.setFile(lastSeen.getFile());
		o.setOffset(lastSeen.getOffset());
		o.setDigest(lastSeen.getDigest());
		o.setHttpCode(lastSeen.getHttpCode());
		o.setMimeType(lastSeen.getMimeType());
		o.setRedirectUrl(lastSeen.getRedirectUrl());
		o.flagDuplicateHTTP(lastSeen.getCaptureTimestamp());
		return FILTER_INCLUDE;
	}

	private int remember(CaptureSearchResult o) {
		lastSeen = o;
		return FILTER_INCLUDE;
	}

	public int filterObject(CaptureSearchResult o) {
		if(o.getFile().equals(EMPTY_VALUE)) {
			if(o.getDigest().equals(EMPTY_SHA1)) {
				return annotate(o);
			}
			return FILTER_INCLUDE;
		}
		return remember(o);
	}

}
