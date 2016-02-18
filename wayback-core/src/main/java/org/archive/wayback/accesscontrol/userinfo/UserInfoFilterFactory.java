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
package org.archive.wayback.accesscontrol.userinfo;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.url.UrlOperations;

/**
 * ExclusionFilterFactory for {@link UserInfoFilter}.
 * @author hunter
 */
public class UserInfoFilterFactory implements
			ExclusionFilterFactory {

	public static class UserInfoFilter extends ExclusionFilter {
		@Override
		public int filterObject(CaptureSearchResult capture) {
			
			// Special case: filter out captures that have userinfo
			boolean hasUserInfo = (UrlOperations.urlToUserInfo(capture.getOriginalUrl()) != null);

			if (hasUserInfo) {
				//make sure this url is available to resolve a revisit but will not show up
				//in calendar page or if directly accessed
				capture.setRobotFlag(CaptureSearchResult.CAPTURE_ROBOT_BLOCKED);
			}
			
			return FILTER_INCLUDE;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.accesscontrol.ExclusionFilterFactory#get()
	 */
	@Override
	public ExclusionFilter get() {
		return new UserInfoFilter();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.accesscontrol.ExclusionFilterFactory#shutdown()
	 */
	@Override
	public void shutdown() {
	}
}
