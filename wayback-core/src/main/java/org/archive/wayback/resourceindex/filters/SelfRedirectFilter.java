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

import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;
import org.archive.wayback.util.url.UrlOperations;

/**
 * SearchResultFilter which INCLUDEs all records, unless they redirect to 
 * themselves, via whatever URL purification schemes are in use.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SelfRedirectFilter implements ObjectFilter<CaptureSearchResult> {
	private static final Logger LOGGER = Logger.getLogger(SelfRedirectFilter
			.class.getName());

	private UrlCanonicalizer canonicalizer = null;
	public SelfRedirectFilter() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}
	public SelfRedirectFilter(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		String httpCode = r.getHttpCode();
		// only filter real 3XX http response codes:
		if(httpCode.startsWith("3")) {
			String redirect = r.getRedirectUrl();
			if(redirect.compareTo("-") != 0) {
				String urlKey = r.getUrlKey();
				try {
					String redirectKey = canonicalizer.urlStringToKey(redirect);
					if((redirectKey != null) && (urlKey != null) &&
							(redirectKey.compareTo(urlKey) == 0)) {
						// only omit if same scheme:
						String origScheme = 
							UrlOperations.urlToScheme(r.getOriginalUrl());
						String redirScheme = 
							UrlOperations.urlToScheme(redirect);
						if((origScheme != null) && (redirScheme != null) &&
								(origScheme.compareTo(redirScheme) == 0)) {
							return FILTER_EXCLUDE;
						}
					}
				} catch (URIException e) {
					// emit message (is that right?) and continue
					LOGGER.info("Bad redirectURL:" + redirect + 
							" urlKey:"+ urlKey +
							" date:"+ r.getCaptureTimestamp());
				}
			}
		}
		return FILTER_INCLUDE;
	}
	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}
	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}
}
