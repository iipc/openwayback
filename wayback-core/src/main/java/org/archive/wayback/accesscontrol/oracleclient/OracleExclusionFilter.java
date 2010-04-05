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
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

/**
 * @author brad
 *
 */
public class OracleExclusionFilter extends ExclusionFilter {
	AccessControlClient client = null;
	private String accessGroup = null;
	
	private final static String POLICY_ALLOW = "allow";
	private final static String POLICY_BLOCK = "block";
	private final static String POLICY_ROBOT = "robots";
	private boolean notifiedRobotSeen = false;
//	private boolean notifiedRobotPassed = false;
	private boolean notifiedAdminSeen = false;
	private boolean notifiedAdminPassed = false;
	
	/**
	 * @param oracleUrl String URL prefix for the Oracle HTTP server
	 * @param accessGroup String group to use with requests to the Oracle
	 */
	public OracleExclusionFilter(String oracleUrl, String accessGroup) {
		this(oracleUrl,accessGroup,null);
	}
	/**
	 * @param oracleUrl String URL prefix for the Oracle HTTP server
	 * @param accessGroup String group to use with requests to the Oracle
	 * @param proxyHostPort String proxyHost:proxyPort to use for robots.txt
	 */
	public OracleExclusionFilter(String oracleUrl, String accessGroup, 
			String proxyHostPort) {
		client = new AccessControlClient(oracleUrl);
		if(proxyHostPort != null) {
		   	int colonIdx = proxyHostPort.indexOf(':');
	    	if(colonIdx > 0) {
	    		String host = proxyHostPort.substring(0,colonIdx);
	    		int port = Integer.valueOf(proxyHostPort.substring(colonIdx+1));
	    		client.setRobotProxy(host, port);
	    	}
		}
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
					if(!notifiedAdminSeen) {
						notifiedAdminSeen = true;
						if(filterGroup != null) {
							filterGroup.setSawAdministrative();
						}
					}
					if(!notifiedAdminPassed) {
						notifiedAdminPassed = true;
						if(filterGroup != null) {
							filterGroup.setPassedAdministrative();
						}
					}
					return FILTER_INCLUDE;
				} else if(policy.equals(POLICY_BLOCK)) {
					if(!notifiedAdminSeen) {
						notifiedAdminSeen = true;
						if(filterGroup != null) {
							filterGroup.setSawAdministrative();
						}
					}
					return FILTER_EXCLUDE;
				} else if(policy.equals(POLICY_ROBOT)) {
					if(!notifiedRobotSeen) {
						notifiedRobotSeen = true;
						if(filterGroup != null) {
							filterGroup.setSawRobots();
						}
					}
					return FILTER_INCLUDE;
//					if(robotFilter != null) {
//						if(!notifiedRobotPassed) {
//							notifiedRobotPassed = true;
//							if(filterGroup != null) {
//								filterGroup.setPassedRobot();
//							}
//						}
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
