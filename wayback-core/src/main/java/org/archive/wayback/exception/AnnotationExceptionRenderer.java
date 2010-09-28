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
package org.archive.wayback.exception;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.accesscontrol.model.Rule;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 * @deprecated
 */
public class AnnotationExceptionRenderer extends BaseExceptionRenderer {
	private AccessControlClient client = null;
	private String oracleUrl = null;
	private String who = null;
	public void init() {
		client = new AccessControlClient(oracleUrl);
	}
//	public String getExceptionHandler(HttpServletRequest httpRequest,
//			HttpServletResponse httpResponse, WaybackRequest wbRequest,
//			WaybackException exception) {
//		// the "standard HTML" response handler:
//		String jspPath = getCustomHandler(exception,wbRequest);
//		if(jspPath == null) {
//			jspPath = super.getExceptionHandler(httpRequest, httpResponse,
//					wbRequest, exception);
//		}
//		return jspPath;
//	}

	private String getCustomHandler(WaybackException e, WaybackRequest wbRequest) {
		String jspPath = null;
		if((e instanceof ResourceNotInArchiveException)
				&& wbRequest.isReplayRequest()) {
			String url = wbRequest.getRequestUrl();
			Date captureDate = wbRequest.getReplayDate();
			try {
				Rule rule = client.getRule(url,captureDate,new Date(),who);
				jspPath = ruleToJspPath(rule);
			} catch (RuleOracleUnavailableException e1) {
				e1.printStackTrace();
			}
		}
		return jspPath;
	}

	private String ruleToJspPath(Rule rule) {
		if(rule != null) {
			String pc = rule.getPublicComment();
			if(pc.startsWith("/")) {
				return pc;
			}
		}
		return null;
	}

	/**
	 * @return the client
	 */
	public AccessControlClient getClient() {
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(AccessControlClient client) {
		client.setRobotLookupsEnabled(false);
		this.client = client;
	}

	/**
	 * @return the oracleUrl
	 */
	public String getOracleUrl() {
		return oracleUrl;
	}

	/**
	 * @param oracleUrl the oracleUrl to set
	 */
	public void setOracleUrl(String oracleUrl) {
		this.oracleUrl = oracleUrl;
		setClient(new AccessControlClient(oracleUrl));
	}

	/**
	 * @return the who
	 */
	public String getWho() {
		return who;
	}

	/**
	 * @param who the who to set
	 */
	public void setWho(String who) {
		this.who = who;
	}
}
