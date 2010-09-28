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
package org.archive.wayback.domainprefix;

import org.archive.wayback.RequestParser;
import org.archive.wayback.requestparser.CompositeRequestParser;
import org.archive.wayback.requestparser.FormRequestParser;
import org.archive.wayback.requestparser.OpenSearchRequestParser;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DomainPrefixCompositeRequestParser extends CompositeRequestParser {
	DomainPrefixRequestParser dprp = new DomainPrefixRequestParser(this);
	protected RequestParser[] getRequestParsers() {
		RequestParser[] theParsers = {
				dprp,
				new OpenSearchRequestParser(this),
				new FormRequestParser(this) 
				};
		return theParsers;
	}
	/**
	 * @param hostPort
	 */
	public void setHostPort(String hostPort) {
		dprp.setHostPort(hostPort);
	}
	/**
	 * @return
	 */
	public String getHostPort() {
		return dprp.getHostPort();
	}
}
