package org.archive.wayback.authenticationcontrol;

import java.util.List;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.IPRange;
import org.archive.wayback.util.operator.BooleanOperator;

public class IPMatchesBooleanOperator implements BooleanOperator<WaybackRequest> {
	private List<IPRange> allowedRanges = null;
	private IPRange range = null;

	public List<IPRange> getAllowedRanges() {
		return allowedRanges;
	}

	public void setAllowedRanges(List<IPRange> allowedRanges) {
		this.allowedRanges = allowedRanges;
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
		return range.contains(ip);
//		for(IPRange range : allowedRanges) {
//			if(range.contains(ip)) {
//				return true;
//			}
//		}
//		return false;
	}

	public IPRange getRange() {
		return range;
	}

	public void setRange(IPRange range) {
		this.range = range;
	}
}
