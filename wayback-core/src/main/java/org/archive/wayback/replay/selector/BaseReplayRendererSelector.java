/* BaseReplayRendererSelector
 *
 * $Id$
 *
 * Created on 11:55:37 AM Jul 18, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.replay.selector;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.ReplayRendererSelector;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class BaseReplayRendererSelector implements ReplayRendererSelector {
	private final static long MAX_HTML_MARKUP_LENGTH = 1024 * 1024 * 5;

	protected long maxSize = MAX_HTML_MARKUP_LENGTH;
	private ReplayRenderer renderer;
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.ReplayRendererSelector#canHandle(org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.CaptureSearchResult, org.archive.wayback.core.Resource)
	 */
	public abstract boolean canHandle(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource);

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.ReplayRendererSelector#getRenderer()
	 */
	public ReplayRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(ReplayRenderer renderer) {
		this.renderer = renderer;
	}
	public boolean isResourceTooBig(Resource resource) {
		return (maxSize > 0) 
			&& (resource.getRecordLength() > maxSize);
	}
	public long getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}
}
