/* IPMatchesBooleanOperator
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.authenticationcontrol;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.IPRange;
import org.archive.wayback.util.operator.BooleanOperator;

public class IPMatchesBooleanOperator implements BooleanOperator<WaybackRequest> {
	private static final Logger LOGGER = Logger.getLogger(IPMatchesBooleanOperator
			.class.getName());
	private List<IPRange> allowedRanges = null;

	public List<String> getAllowedRanges() {
		return null;
	}

	public void setAllowedRanges(List<String> allowedRanges) {
		this.allowedRanges = new ArrayList<IPRange>();
		for(String ip : allowedRanges) {
			IPRange range = new IPRange();
			if(range.setRange(ip)) {
				this.allowedRanges.add(range);
			} else {
				LOGGER.error("Unable to parse range (" + ip + ")");
			}
		}
	}

	public boolean isTrue(WaybackRequest value) {
		if(allowedRanges == null) {
			return false;
		}
		String ipString = value.getRemoteIPAddress();
		if(ipString == null) {
			return false;
		}
		byte[] ip = IPRange.matchIP(ipString);
		for(IPRange range : allowedRanges) {
			if(range.contains(ip)) {
				return true;
			}
		}
		return false;
	}
}
