/* RobotMetaFlags
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
