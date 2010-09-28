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
package org.archive.wayback.resourcestore.indexer;

import org.archive.wayback.core.CaptureSearchResult;

public class RobotMetaFlags {
	private static String NO_NOTHIN_MATCH = "NONE";
	private static String NO_FOLLOW_MATCH = "NOFOLLOW";
	private static String NO_INDEX_MATCH = "NOINDEX";
	private static String NO_ARCHIVE_MATCH = "NOARCHIVE";
	
	private boolean noArchive = false;
	private boolean noIndex = false;
	private boolean noFollow = false;
	public void reset() {
		noArchive = false;
		noIndex = false;
		noFollow = false;
	}
	public void parse(String content) {
		if(content == null) {
			return;
		}
		String up = content.replaceAll("-", "").toUpperCase();
		if(up.contains(NO_FOLLOW_MATCH)) {
			noFollow = true;
		}
		if(up.contains(NO_ARCHIVE_MATCH)) {
			noArchive = true;
		}
		if(up.contains(NO_INDEX_MATCH)) {
			noIndex = true;
		}
		if(up.contains(NO_NOTHIN_MATCH)) {
			noFollow = true;
			noArchive = true;
			noIndex = true;
		}
	}
	public void apply(CaptureSearchResult result) {
		if(noFollow) result.setRobotNoFollow();
		if(noIndex) result.setRobotNoIndex();
		if(noArchive) result.setRobotNoArchive();
	}
}
