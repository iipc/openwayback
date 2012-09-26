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
	
	public boolean canHandle(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource) {
		return canHandle(wbRequest, result, resource, resource);
	}

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
