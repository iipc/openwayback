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

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Selects {@link ReplayRenderer} by Content-Type.
 * Sources of Content-Type (higher priority to lower):
 * <ul>
 * <li>{@link WaybackRequest#getForcedContentType()} (set by context flags)</li>
 * <li>{@link CaptureSearchResult#getMimeType()} (from index, {@code warc/revisit} is ignored)</li>
 * <li>{@link CaptureSearchResult#getMimeType()} of {@code duplicatePayload}</li>
 * <li>{@code Content-Type} header of payload {@link Resource}</li>
 * </ul>
 * <p>1.8.1 2014-06-17 {@code getForcedContentType()} is added.
 * This makes following selectors optional:
 * <ul>
 * <li>{@code CSSRequestSelector}</li>
 * <li>{@code JSRequestSelector}</li>
 * </ul>
 * @author brad
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

		String mime = wbRequest.getForcedContentType();

		if (mime == null)
			mime = result.getMimeType();
		
		if ((mime == null) || mime.equals(AccessPoint.REVISIT_STR)) {
			if (result.getDuplicatePayload() != null) {
				mime = result.getDuplicatePayload().getMimeType();
			} else {
				mime = payloadResource.getHeader("Content-Type");
			}
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
