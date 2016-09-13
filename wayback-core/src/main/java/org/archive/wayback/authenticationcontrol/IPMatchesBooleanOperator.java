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
package org.archive.wayback.authenticationcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.IPRange;
import org.archive.wayback.util.operator.BooleanOperator;

/**
 * A BooleanOperator which results in true value if a users request originated
 * from within a list of configured IP ranges.
 * @author brad
 *
 */
public class IPMatchesBooleanOperator implements BooleanOperator<WaybackRequest> {
	private static final Logger LOGGER = Logger.getLogger(IPMatchesBooleanOperator
			.class.getName());
	private List<IPRange> allowedRanges = null;

	/**
	 * @return null. this is a placeholder for Spring's getter/setter 
	 * 			examination
	 */
	public List<String> getAllowedRanges() {
		return null;
	}

	/**
	 * @param allowedRanges parses each String IPRange provided, added them to
	 * 		the list of IPRanges which this operator matches
	 */
	public void setAllowedRanges(List<String> allowedRanges) {
		this.allowedRanges = new ArrayList<IPRange>();
		for(String ip : allowedRanges) {
			IPRange range = new IPRange();
			if(range.setRange(ip)) {
				this.allowedRanges.add(range);
			} else {
				LOGGER.severe("Unable to parse range (" + ip + ")");
			}
		}
	}

	private List<IPRange> proxyIPList = null;
	public List<String> getProxyIPList() {
		return null;
	}

	/**
	 * @param proxyIPList parses each String IPRange provided for the proxies, adding them to
	 * 		the list of IPRanges which must be ignored by the IP match operator
	 */
	public void setProxyIPList(List<String> proxyIPList) {
		this.proxyIPList = new ArrayList<IPRange>();
		for (String ip : proxyIPList) {
			IPRange range = new IPRange();
			if (range.setRange(ip)) {
				this.proxyIPList.add(range);
			} else {
				LOGGER.severe("Unable to parse range (" + ip + ")");
			}
		}
	}


	public String getClientIPFromForwardedForHeader(String forwardedForHeader, String remoteAddr){
	private static final Logger LOGGER = Logger.getLogger(IPMatchesBooleanOperator
			.class.getName());

		if (proxyIPList.isEmpty()) {
			return remoteAddr;
		}

		ArrayList<String> forwardingIPs;
		String ip;
		if (forwardedForHeader.contains(",")) {
			forwardingIPs =  Collections.reverse(Arrays.asList(forwardedForHeader.split(",")));
			for (String forwardingIP : forwardingIPs){
				if (proxyIPList.contains(forwardingIP)){
					continue;
				}
				ip = forwardingIP;
				break;
			}
		} else {
			ip = forwardedForHeader;
		}
		return ip;
	}

	public boolean isTrue(WaybackRequest value) {
		if(allowedRanges == null) {
			return false;
		}
		String ipString = value.getRemoteIPAddress();
		String forwardedForHeader = value.getForwardedForHeader();
		if (!proxyIPList.isEmpty()) {
			ipString = getClientIPFromForwardedForHeader(forwardedForHeader, ipString);
		}
		if(ipString == null) {
			return false;
		}
		byte[] ip = IPRange.matchIP(ipString);
		if(ip == null) {
			LOGGER.severe("Unable to parse remote IP address("+ipString+")");
		} else {
			for(IPRange range : allowedRanges) {
				if(range.contains(ip)) {
					if(LOGGER.isLoggable(Level.FINE)){
						LOGGER.fine(String.format("Range(%s) matched(%s)",
								range.getOriginal(),ipString));
					}
					return true;
				} else {
					if(LOGGER.isLoggable(Level.FINE)){
						LOGGER.fine(String.format("Range(%s) NO match(%s)",
								range.getOriginal(),ipString));
					}
				}
			}
		}
		return false;
	}
}
