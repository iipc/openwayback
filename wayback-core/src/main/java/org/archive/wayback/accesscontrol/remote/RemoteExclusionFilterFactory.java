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
package org.archive.wayback.accesscontrol.remote;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

/**
 *
 *
 * @deprecated superseded by ExclusionOracle
 * @author brad
 * @version $Date$, $Revision$
 */
public class RemoteExclusionFilterFactory implements ExclusionFilterFactory {

	private String exclusionUrlPrefix = null;

	private String exclusionUserAgent = null;

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.ExclusionFilterFactory#get()
	 */
	public ExclusionFilter get() {
		return new RemoteExclusionFilter(exclusionUrlPrefix, exclusionUserAgent);
	}

	/**
	 * @return the exclusionUrlPrefix
	 */
	public String getExclusionUrlPrefix() {
		return exclusionUrlPrefix;
	}

	/**
	 * @param exclusionUrlPrefix the exclusionUrlPrefix to set
	 */
	public void setExclusionUrlPrefix(String exclusionUrlPrefix) {
		this.exclusionUrlPrefix = exclusionUrlPrefix;
	}

	/**
	 * @return the exclusionUserAgent
	 */
	public String getExclusionUserAgent() {
		return exclusionUserAgent;
	}

	/**
	 * @param exclusionUserAgent the exclusionUserAgent to set
	 */
	public void setExclusionUserAgent(String exclusionUserAgent) {
		this.exclusionUserAgent = exclusionUserAgent;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.accesscontrol.ExclusionFilterFactory#shutdown()
	 */
	public void shutdown() {
		// nothing to do..
	}


}
