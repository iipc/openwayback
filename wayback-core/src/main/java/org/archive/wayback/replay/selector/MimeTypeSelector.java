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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.AccessPoint;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class MimeTypeSelector extends BaseReplayRendererSelector {
	private Map<String, Object> mimeMatches = null;
	private List<String> mimeContains = null;

	@Override
	public boolean canHandle(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource) {

		if (isResourceTooBig(payloadResource)) {
			return false;
		}
		String mime = result.getMimeType();
		
		if ((mime == null) || mime.equals(AccessPoint.REVISIT_STR)) {
			mime = payloadResource.getHttpHeaders().get("Content-Type");
		}
		
		if (mime == null) {
			mime = "unk";
		}
		
		if (mimeMatches != null) {
			if (mimeMatches.containsKey(mime)) {
				return true;
			}
		}
		if (mimeContains != null) {
			for (String contains : mimeContains) {
				if (mime.indexOf(contains) != -1) {
					return true;
				}
			}
		}
		return false;
	}

	public void setMimeMatches(List<String> mimes) {
		mimeMatches = new HashMap<String, Object>();
		for (String mime : mimes) {
			mimeMatches.put(mime, null);
		}
	}

	public List<String> getMimeMatches() {
		return null;
	}

	public void setMimeContains(List<String> mimes) {
		mimeContains = mimes;
	}

	public List<String> getMimeContains() {
		return mimeContains;
	}
}
