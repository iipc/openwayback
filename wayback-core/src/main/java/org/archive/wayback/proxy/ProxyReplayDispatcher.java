/* ProxyReplayRendererDispatcher
 *
 * $Id$
 *
 * Created on 11:39:11 AM Aug 9, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.proxy;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.BaseReplayDispatcher;
import org.archive.wayback.replay.TransparentReplayRenderer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ProxyReplayDispatcher extends BaseReplayDispatcher {

	private ReplayRenderer renderer = new TransparentReplayRenderer();
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.ReplayRendererDispatcher#getRenderer(org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResult, org.archive.wayback.core.Resource)
	 */
	@Override
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			SearchResult result, Resource resource) {
		// always use the transparent:
		return renderer;
	}
}
