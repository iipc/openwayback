/* PathMatchSelector
 *
 * $Id$
 *
 * Created on 12:19:54 PM Jul 18, 2008.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class PathMatchSelector extends BaseReplayRendererSelector {

	private List<String> pathContains = null;
	private List<String> queryContains = null;
	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.selector.BaseReplayRendererSelector#canHandle(org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.CaptureSearchResult, org.archive.wayback.core.Resource)
	 */
	@Override
	public boolean canHandle(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource) {
		if(isResourceTooBig(resource)) {
			return false;
		}
		try {
			URL url = new URL(result.getOriginalUrl());
			if(pathContains != null) {
				String path = url.getPath();
				for(String test : pathContains) {
					if(path.indexOf(test) != -1) {
						return true;
					}
				}
			}
			if(queryContains != null) {
				String query = url.getQuery();
				if(query != null) {
					for(String test : queryContains) {
						if(query.indexOf(test) != -1) {
							return true;
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			// just eat it.
		}
		return false;
	}
	public List<String> getPathContains() {
		return pathContains;
	}
	public void setPathContains(List<String> pathContains) {
		this.pathContains = pathContains;
	}
	public List<String> getQueryContains() {
		return queryContains;
	}
	public void setQueryContains(List<String> queryContains) {
		this.queryContains = queryContains;
	}
}
