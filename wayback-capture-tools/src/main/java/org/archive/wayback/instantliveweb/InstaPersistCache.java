package org.archive.wayback.instantliveweb;

import org.archive.format.cdx.CDXInputSource;
import org.archive.wayback.core.CaptureSearchResult;

public interface InstaPersistCache extends CDXInputSource {

	public boolean saveResult(CaptureSearchResult result);
}
