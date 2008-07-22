/* SelectorReplayDispatcher
 *
 * $Id$
 *
 * Created on 11:46:40 AM Jul 18, 2008.
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
package org.archive.wayback.replay;

import java.util.List;

import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SelectorReplayDispatcher implements ReplayDispatcher {
	private List<ReplayRendererSelector> selectors = null;
	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayDispatcher#getRenderer(org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.CaptureSearchResult, org.archive.wayback.core.Resource)
	 */
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource) {
		if(selectors != null) {
			for(ReplayRendererSelector selector : selectors) {
				if(selector.canHandle(wbRequest, result, resource)) {
					return selector.getRenderer();
				}
			}
		}
		return null;
	}
	public List<ReplayRendererSelector> getSelectors() {
		return selectors;
	}
	public void setSelectors(List<ReplayRendererSelector> selectors) {
		this.selectors = selectors;
	}

}
