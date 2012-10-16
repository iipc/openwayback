package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;

public class ClosestResultURLAndDateTrackingFilter extends
		ClosestResultTrackingFilter {
	
	protected String origUrl;
	
	public ClosestResultURLAndDateTrackingFilter(long wantMS, String origUrl) {
		super(wantMS);
		this.origUrl = origUrl;
	}
	
	public int filterObject(CaptureSearchResult o) {
		
		if(found) {
			// dates are now getting further from desired dates, as an 
			// optimization, skip the math: 
			return FILTER_INCLUDE;
		}
		long captureMS = o.getCaptureDate().getTime();
		long diffMS = Math.abs(captureMS - wantMS);

		if(closest == null) {
			// first result to pass, by definition, for now it's the closest:
			closest = o;
			closestDiffMS = diffMS;
			
		} else {
			
			if(closestDiffMS < diffMS) {
				// dates now increasing, start short-circuiting the rest
				found = true;
				// See if the exact (uncanonicalized) url matches, if so, prefer this one
			} else if ((closestDiffMS == diffMS) && (origUrl != null)) {
				if (closest.getOriginalUrl().equals(origUrl)) {
					found = true;
				} else {
					closest = o;
					closestDiffMS = diffMS;					
				}				
			} else {
				// this is closer than anything we've seen:
				closest = o;
				closestDiffMS = diffMS;
			}
		}
		return FILTER_INCLUDE;
	}
}
