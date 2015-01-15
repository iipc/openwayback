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
package org.archive.wayback.archivalurl;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.replay.DateRedirectingClosestResultSelector;
import org.archive.wayback.replay.DefaultClosestResultSelector;

/**
 * ClosestResultSelector that redirects to Archival-URL replay URL
 * for the capture given if its timestamp is different from the request.
 * <p>This class preserves context flag upon redirect, but can cause
 * redirect loop.</p>
 * @author brad
 * @deprecated 1.8.1 2014-07-2 no replacement.
 * use {@link DefaultClosestResultSelector}.
 */
public class ArchivalUrlDateRedirectingClosestResultSelector 
extends DateRedirectingClosestResultSelector {
	protected void doRedirection(WaybackRequest wbRequest, 
			CaptureSearchResult closest) throws BetterRequestException {
		// redirect to the better version:
		ArchivalUrl aUrl = new ArchivalUrl(wbRequest);
		String betterUrl = wbRequest.getAccessPoint().getReplayPrefix() + 
			aUrl.toString(closest.getCaptureTimestamp(), 
				closest.getOriginalUrl());
		throw new BetterRequestException(betterUrl);
	}
}
