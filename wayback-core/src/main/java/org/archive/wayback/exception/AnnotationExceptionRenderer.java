/* AnnotationExceptionRenderer
 *
 * $Id$
 *
 * Created on 7:17:24 PM Jun 10, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 */
public class AnnotationExceptionRenderer extends BaseExceptionRenderer {
	private AccessControlClient client = null;
	private String oracleUrl = null;
	private String who = null;
	public void init() {
		client = new AccessControlClient(oracleUrl);
	}
	public String getExceptionHandler(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception) {
		// the "standard HTML" response handler:
		String jspPath = getCustomHandler(exception,wbRequest);
		if(jspPath == null) {
			jspPath = super.getExceptionHandler(httpRequest, httpResponse,
					wbRequest, exception);
		}
		return jspPath;
	}

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
