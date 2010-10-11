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
package org.archive.wayback.replay;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;

/**
 * @author brad
 *
 */
public class DateRedirectingClosestResultSelector 
implements ClosestResultSelector {

	public CaptureSearchResult getClosest(WaybackRequest wbRequest,
			CaptureSearchResults results) throws BetterRequestException {

		CaptureSearchResult closest = results.getClosest();
		String reqDateStr = wbRequest.getReplayTimestamp();
		String resDateStr = closest.getCaptureTimestamp();
		
		boolean doRedirect = false;
		
		// if the request date is shorter than the result date, always redirect:
		if(reqDateStr.length() < resDateStr.length()) {
			doRedirect = true;
		} else {
			// if the result is not for the exact date requested, redirect to the
			// exact date. some capture dates are not 14 digits, only compare as 
			// many digits as are in the result date:
			if(!resDateStr.equals(reqDateStr.substring(0,resDateStr.length()))) {
				doRedirect = true;
			}
		}
		if(doRedirect) {
			doRedirection(wbRequest,closest);
		}
		return closest;
	}
	protected void doRedirection(WaybackRequest wbRequest, 
			CaptureSearchResult closest) throws BetterRequestException {
		// redirect to the better version:
		String url = closest.getOriginalUrl();
		String captureDate = closest.getCaptureTimestamp();
		ResultURIConverter uriConverter = 
			wbRequest.getAccessPoint().getUriConverter();
		String betterURI = uriConverter.makeReplayURI(captureDate,url);
		throw new BetterRequestException(betterURI);
	}
}
