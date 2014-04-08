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
package org.archive.wayback.archivalurl;

import org.archive.wayback.ResultURIConverter;

/**
 * Wraps around an ArchivalUrlResultURIConverter, to add flags after the
 * datespec for a specific context ("js_" for javascript, "cs_" for CSS, etc).
 * <p>Actually this class does not wrap ArchivalUrlResultURIConverter.  It only saves
 * replayURIPrefix of ArchivalUrlResultURIConverter given, and translates URL on its own.</p>
 * <p>For use in archival-URL mode.</p>
 * <p>Possible Refactoring: may change to an inner class of {@link ArchivalUrlContextResultURIConverterFactory},
 * the sole user of this class, or other class.</p>
 * @author brad
 * @version $Date$, $Revision$
 */

public class ArchivalUrlSpecialContextResultURIConverter 
implements ResultURIConverter {
	

	private String replayURIPrefix = null;
	private String context;
	
	/**
	 * @param converter ArchivalUrlResultURIConverter to wrap
	 * @param context flags indicating the context of URLs created by this 
	 * 				object
	 */
	public ArchivalUrlSpecialContextResultURIConverter(
			ArchivalUrlResultURIConverter converter, String context) {
		replayURIPrefix = converter.getReplayURIPrefix();
		this.context = context;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		String suffix = datespec + context + "/" + url;
		if(replayURIPrefix == null) {
			return suffix;
		} else {
			if(url.startsWith(replayURIPrefix)) {
				return url;
			}
			return replayURIPrefix + suffix;
		}
	}
}
