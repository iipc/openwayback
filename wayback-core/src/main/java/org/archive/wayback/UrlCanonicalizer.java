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
package org.archive.wayback;

import org.apache.commons.httpclient.URIException;

/**
 * Interface for implementations that transform an input String URL into a
 * canonical form, suitable for lookups in a ResourceIndex. URLs should be sent
 * through the same canonicalizer they will be searched using, before being 
 * inserted into a ResourceIndex.
 * @author brad
 *
 */
public interface UrlCanonicalizer {
	/**
	 * @param url String representation of an URL, in as original, and 
	 * 		unchanged form as possible.
	 * @return a lookup key appropriate for searching within a ResourceIndex.
	 * @throws URIException if the input url String is not a valid URL.
	 */
	public String urlStringToKey(String url) throws URIException;
	
	/** 
	 * Returns true if this Canonicalizer returns SURTs, false is URLs
	 * @return
	 */
	public boolean isSurtForm();
}
