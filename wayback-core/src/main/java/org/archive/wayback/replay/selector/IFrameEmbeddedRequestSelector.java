package org.archive.wayback.replay.selector;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;

public class IFrameEmbeddedRequestSelector extends BaseReplayRendererSelector {

	@Override
	public boolean canHandle(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource) {
		return wbRequest.isIFrameWrapperContext();
	}

}
