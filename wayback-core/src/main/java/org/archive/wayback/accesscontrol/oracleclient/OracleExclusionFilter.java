package org.archive.wayback.accesscontrol.oracleclient;

import java.util.Date;

import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.RobotsUnavailableException;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class OracleExclusionFilter implements ObjectFilter<CaptureSearchResult> {
	AccessControlClient client = null;
	private String accessGroup = null;
	
	private final static String POLICY_ALLOW = "allow";
	private final static String POLICY_BLOCK = "block";
	private final static String POLICY_ROBOT = "robots";
	
	
	public OracleExclusionFilter(String oracleUrl, String accessGroup) {
		client = new AccessControlClient(oracleUrl);
		this.accessGroup = accessGroup;
	}
	
	
	public int filterObject(CaptureSearchResult o) {
		String url = o.getOriginalUrl();
		Date captureDate = o.getCaptureDate();
		Date retrievalDate = new Date();
		
		String policy;
		try {
			policy = client.getPolicy(ArchiveUtils.addImpliedHttpIfNecessary(url), captureDate, retrievalDate, 
					accessGroup);
			if(policy != null) {
				if(policy.equals(POLICY_ALLOW)) {
					return FILTER_INCLUDE;
				} else if(policy.equals(POLICY_BLOCK)) {
					return FILTER_EXCLUDE;
				} else if(policy.equals(POLICY_ROBOT)) {
					return FILTER_INCLUDE;
//					if(robotFilter != null) {
//						return robotFilter.filterObject(o);
//					} else {
//						return FILTER_EXCLUDE;
//					}
				}
			}
		} catch (RobotsUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuleOracleUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return FILTER_EXCLUDE;			
	}
}
