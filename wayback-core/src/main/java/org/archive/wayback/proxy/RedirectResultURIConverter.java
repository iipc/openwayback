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
package org.archive.wayback.proxy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RedirectResultURIConverter implements ResultURIConverter {
	
	private String redirectURI = null;

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		String res = null;
		if(!url.startsWith(WaybackConstants.HTTP_URL_PREFIX)) {
			url = WaybackConstants.HTTP_URL_PREFIX + url;
		}
        try {
			res = redirectURI + "?url=" + URLEncoder.encode(url, "UTF-8") + 
				"&time=" + URLEncoder.encode(datespec, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should not be able to happen -- with hard-coded UTF-8, anyways..
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * @param redirectURI the redirectURI to set
	 */
	public void setRedirectURI(String redirectURI) {
		this.redirectURI = redirectURI;
	}
}
