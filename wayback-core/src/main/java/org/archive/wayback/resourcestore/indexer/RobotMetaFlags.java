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
