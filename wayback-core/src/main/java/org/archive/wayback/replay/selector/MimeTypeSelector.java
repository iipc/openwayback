/* MimeTypeSelector
 *
 * $Id$
 *
 * Created on 12:04:56 PM Jul 18, 2008.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class MimeTypeSelector extends BaseReplayRendererSelector {
	private Map<String,Object> mimeMatches = null;
	private List<String> mimeContains = null;
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.selector.BaseReplayRendererSelector#canHandle(org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.CaptureSearchResult, org.archive.wayback.core.Resource)
	 */
	@Override
	public boolean canHandle(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource) {
		if(isResourceTooBig(resource)) {
			return false;
		}
		String mime = result.getMimeType();
		if(mimeMatches != null) {
			if(mimeMatches.containsKey(mime)) {
				return true;
			}
		}
		if(mimeContains != null) {
			for(String contains : mimeContains) {
				if(mime.indexOf(contains) != -1) {
					return true;
				}
			}
		}
		return false;
	}
	public void setMimeMatches(List<String> mimes) {
		mimeMatches = new HashMap<String,Object>();
		for(String mime : mimes) {
			mimeMatches.put(mime,null);
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
