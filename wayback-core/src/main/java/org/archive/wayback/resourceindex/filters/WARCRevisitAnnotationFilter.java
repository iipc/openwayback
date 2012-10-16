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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * Filter class that observes a stream of SearchResults tracking for each
 * complete record, a mapping of that records Digest to:
 *   Arc/Warc Filename
 * 	 Arc/Warc offset
 *   HTTP Response
 *   MIME-Type
 *   Redirect URL
 *   
 * If subsequent SearchResults are missing these fields ("-") and the Digest 
 * field is in the map, then the SearchResults missing fields are replaced with 
 * the values from the previously seen record with the same digest, and an 
 * additional annotation field is added.
 * 
 * @author brad
 * @version $Date: 2011-11-28 22:03:59 -0800 (Mon, 28 Nov 2011) $, $Revision: 3574 $
 */
public class WARCRevisitAnnotationFilter 
implements ObjectFilter<CaptureSearchResult> {
	
	private final static String EMPTY_VALUE = "-";
	private final static String REVISIT_VALUE = "warc/revisit";
	
	private static final Logger LOGGER = Logger.getLogger(
			WARCRevisitAnnotationFilter.class.getName());	

	private HashMap<String,CaptureSearchResult> memory = null;

	public WARCRevisitAnnotationFilter() {
		memory = new HashMap<String,CaptureSearchResult>();
	}

	private int annotate(CaptureSearchResult o) {
		o.flagDuplicateDigest();
		
		String thisDigest = o.getDigest();
		CaptureSearchResult last = memory.get(thisDigest);
		if (last == null) {
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer("did not find matching digest in previous fetch of url, hopefully it's a new-style revisit - "
						+ o.getCaptureTimestamp() + " " + o.getOriginalUrl());
			}
			return FILTER_INCLUDE;
		}
		
		o.flagDuplicateDigest(last);
		
		return FILTER_INCLUDE;
	}

	private int remember(CaptureSearchResult o) {
		memory.put(o.getDigest(),o);
		return FILTER_INCLUDE;
	}

//	public CaptureSearchResult adapt(CaptureSearchResult o) {
//		if(o.getFile().equals(EMPTY_VALUE)
//				|| o.getMimeType().equals(REVISIT_VALUE)) {
//			return annotate(o);
//		}
//		return remember(o);
//	}

	public int filterObject(CaptureSearchResult o) {
		if(o.getFile().equals(EMPTY_VALUE)
				|| o.getMimeType().equals(REVISIT_VALUE)) {
			return annotate(o);
		}
		return remember(o);
	}
}
