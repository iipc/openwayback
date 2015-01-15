/*  This file is part of the Wayback archival access software
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
package org.archive.wayback.accesscontrol.oracleclient;

import java.util.Date;

import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.RobotsUnavailableException;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

import com.sleepycat.je.tree.SearchResult;

/**
 * {@link ExclusionFilter} implementation that queries remote "Exclusion Oracle"
 * with {@link AccessControlClient} to determine which {@link SearchResult}s can
 * be exposed.
 * @author brad
 * @see OracleExclusionFilterFactory
 * @see AccessControlClient
 */
public class OracleExclusionFilter extends ExclusionFilter {

	protected AccessControlClient client = null;
	protected String accessGroup = null;

	private final static String POLICY_ALLOW = "allow";
	private final static String POLICY_BLOCK = "block";
	private final static String POLICY_ROBOT = "robots";
	private boolean notifiedRobotSeen = false;
	private boolean notifiedAdminSeen = false;
	private boolean notifiedAdminPassed = false;

	/**
	 * @param oracleUrl String URL prefix for the Oracle HTTP server
	 * @param accessGroup String group to use with requests to the Oracle
	 */
	public OracleExclusionFilter(String oracleUrl, String accessGroup) {
		this(oracleUrl, accessGroup, null);
	}

	/**
	 * @param oracleUrl String URL prefix for the Oracle HTTP server
	 * @param accessGroup String group to use with requests to the Oracle
	 * @param proxyHostPort String proxyHost:proxyPort to use for robots.txt
	 */
	public OracleExclusionFilter(String oracleUrl, String accessGroup,
			String proxyHostPort) {
		initializeClient(oracleUrl, proxyHostPort);
		this.accessGroup = accessGroup;
	}

	/**
	 * Initialize with AccessControlClient and access group.
	 * @param AccessControlClient pre-initialized access control client
	 * @param accessGroup access group
	 */
	public OracleExclusionFilter(AccessControlClient client, String accessGroup) {
		this.accessGroup = accessGroup;
		this.client = client;
	}

	protected void initializeClient(String oracleUrl, String proxyHostPort) {
		client = new AccessControlClient(oracleUrl);
		if (proxyHostPort != null) {
			int colonIdx = proxyHostPort.indexOf(':');
			if (colonIdx > 0) {
				String host = proxyHostPort.substring(0, colonIdx);
				int port = Integer.valueOf(proxyHostPort
					.substring(colonIdx + 1));
				client.setRobotProxy(host, port);
			}
		}
	}

	protected int handleAllow() {
		if (!notifiedAdminSeen) {
			notifiedAdminSeen = true;
			if (filterGroup != null) {
				filterGroup.setSawAdministrative();
			}
		}
		if (!notifiedAdminPassed) {
			notifiedAdminPassed = true;
			if (filterGroup != null) {
				filterGroup.setPassedAdministrative();
			}
		}
		return FILTER_INCLUDE;
	}

	protected int handleBlock() {
		if (!notifiedAdminSeen) {
			notifiedAdminSeen = true;
			if (filterGroup != null) {
				filterGroup.setSawAdministrative();
			}
		}
		if (!notifiedAdminPassed) {
			notifiedAdminPassed = true;
			if (filterGroup != null) {
				filterGroup.setPassedAdministrative(false);
			}
		}
		return FILTER_EXCLUDE;
	}

	protected int handleRobots() {
		if (!notifiedRobotSeen) {
			notifiedRobotSeen = true;
			if (filterGroup != null) {
				filterGroup.setSawRobots();
			}
		}
		return FILTER_INCLUDE;
	}

	public int filterObject(CaptureSearchResult o) {
		String url = o.getOriginalUrl();
		Date captureDate = o.getCaptureDate();
		Date retrievalDate = new Date();

		String policy;
		try {
			policy = client.getPolicy(
				ArchiveUtils.addImpliedHttpIfNecessary(url), captureDate,
				retrievalDate, accessGroup);
			if (policy != null) {
				if (policy.equals(POLICY_ALLOW)) {
					return handleAllow();
				} else if (policy.equals(POLICY_BLOCK)) {
					return handleBlock();
				} else if (policy.equals(POLICY_ROBOT)) {
					return handleRobots();
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
