package org.archive.wayback.authenticationcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.archive.wayback.WaybackConstants;
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
				LOGGER.severe("Unable to parse range (" + ip + ")");
			}
		}
	}

	public boolean isTrue(WaybackRequest value) {
		if(allowedRanges == null) {
			return false;
		}
		String ipString = value.get(WaybackConstants.REQUEST_REMOTE_ADDRESS);
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
