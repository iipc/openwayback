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
