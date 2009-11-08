/* OracleExclusionFilter
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
package org.archive.wayback.accesscontrol.oracleclient;

import java.util.Date;

import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.RobotsUnavailableException;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * @author brad
 *
 */
public class OracleExclusionFilter implements ObjectFilter<CaptureSearchResult> {
	AccessControlClient client = null;
	private String accessGroup = null;
	
	private final static String POLICY_ALLOW = "allow";
	private final static String POLICY_BLOCK = "block";
	private final static String POLICY_ROBOT = "robots";
	
	
	/**
	 * @param oracleUrl String URL prefix for the Oracle HTTP server
	 * @param accessGroup String group to use with requests to the Oracle
	 */
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
			e.printStackTrace();
		} catch (RuleOracleUnavailableException e) {
			e.printStackTrace();
		}
		return FILTER_EXCLUDE;			
	}
}
