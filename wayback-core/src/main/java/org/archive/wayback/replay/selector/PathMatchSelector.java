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
 * Class which allows matching based on:
 * 
 * a) one of several strings, any of which being found in the path cause match
 * b) one of several strings, any of which being found in the query cause match
 * c) one of several strings, *ALL* of which being found in the url cause match
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class PathMatchSelector extends BaseReplayRendererSelector {

	private List<String> pathContains = null;
	private List<String> queryContains = null;
	private List<String> urlContainsAll = null;

	@Override
	public boolean canHandle(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource) {

		if (isResourceTooBig(payloadResource)) {
			return false;
		}
		try {
			URL url = new URL(result.getOriginalUrl());
			if(urlContainsAll != null) {
				String path = url.toString();
				for(String test : urlContainsAll) {
					if(path.indexOf(test) == -1) {
						return false;
					}
				}
				return true;
			}
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
	/**
	 * @return list of Strings, any of which being found in the path cause a
	 * match
	 */
	public List<String> getPathContains() {
		return pathContains;
	}
	/**
	 * @param pathContains list of Strings, any of which being found in the 
	 * path cause a match
	 */
	public void setPathContains(List<String> pathContains) {
		this.pathContains = pathContains;
	}
	/**
	 * @return list of Strings, *ALL* of which must be found somewhere in the
	 * URL to cause a match
	 */
	public List<String> getUrlContainsAll() {
		return urlContainsAll;
	}
	/**
	 * @param urlContainsAll list of Strings, *ALL* of which must be found 
	 * somewhere in the URL to cause a match
	 */
	public void setUrlContainsAll(List<String> urlContainsAll) {
		this.urlContainsAll = urlContainsAll;
	}
	/**
	 * @return list of Strings, any of which being found in the query cause a
	 * match
	 */
	public List<String> getQueryContains() {
		return queryContains;
	}
	/**
	 * @param queryContains list of Strings, any of which being found in the 
	 * query cause a match
	 */
	public void setQueryContains(List<String> queryContains) {
		this.queryContains = queryContains;
	}
}
