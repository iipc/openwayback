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
package org.archive.wayback.util.url;

import org.apache.commons.httpclient.URIException;
import org.archive.url.URLKeyMaker;
import org.archive.url.WaybackURLKeyMaker;
import org.archive.wayback.UrlCanonicalizer;

/**
 * UrlCanonicalizer which allows use of the SURT canonicalization code from 
 * archive-commons.
 * 
 * @author brad
 *
 */
public class KeyMakerUrlCanonicalizer implements UrlCanonicalizer {
	URLKeyMaker keyMaker;
	private boolean surtForm;
	/**
	 * 
	 */
	public KeyMakerUrlCanonicalizer() {
		this.surtForm = true;
		keyMaker = new WaybackURLKeyMaker(true);
	}

	public KeyMakerUrlCanonicalizer(boolean surtForm) {
		this.surtForm = surtForm;
		keyMaker = new WaybackURLKeyMaker(surtForm);
	}
	
	public String urlStringToKey(String url) throws URIException {
		return keyMaker.makeKey(url);
	}
	/**
	 * @return the keyMaker
	 */
	public URLKeyMaker getKeyMaker() {
		return keyMaker;
	}
	/**
	 * @param keyMaker the keyMaker to set
	 */
	public void setKeyMaker(URLKeyMaker keyMaker) {
		this.keyMaker = keyMaker;
	}
	
	public boolean isSurtForm() {
		return surtForm;
	}
}
