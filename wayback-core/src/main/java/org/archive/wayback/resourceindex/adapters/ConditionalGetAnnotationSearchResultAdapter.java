/* ConditionalGetAnnotationSearchResultAdapter
 *
 * $Id$
 *
 * Created on 6:09:05 PM Mar 12, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
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
package org.archive.wayback.resourceindex.adapters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;

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

public class ConditionalGetAnnotationSearchResultAdapter
implements Adapter<CaptureSearchResult,CaptureSearchResult> {
	
	private final static String EMPTY_VALUE = "-";
	private final static String EMPTY_SHA1 = "3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ";

	private CaptureSearchResult lastSeen = null;

	public ConditionalGetAnnotationSearchResultAdapter() {
	}

	private CaptureSearchResult annotate(CaptureSearchResult o) {
		if(lastSeen == null) {
			// TODO: log missing record digest reference
			return null;
		}
		o.setFile(lastSeen.getFile());
		o.setOffset(lastSeen.getOffset());
		o.setDigest(lastSeen.getDigest());
		o.setHttpCode(lastSeen.getHttpCode());
		o.setMimeType(lastSeen.getMimeType());
		o.setRedirectUrl(lastSeen.getRedirectUrl());
		o.flagDuplicateHTTP(lastSeen.getCaptureTimestamp());
		return o;
	}

	private CaptureSearchResult remember(CaptureSearchResult o) {
		lastSeen = o;
		return o;
	}

	public CaptureSearchResult adapt(CaptureSearchResult o) {
		if(o.getFile().equals(EMPTY_VALUE)) {
			if(o.getDigest().equals(EMPTY_SHA1)) {
				return annotate(o);
			}
			return o;
		}
		return remember(o);
	}
}
