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
package org.archive.wayback.requestparser;

/**
 * Abstract subclass of BaseRequestParser, which allows retrieving
 * configured maxRecords, and earliest and latest timestamp config from an
 * delegate instance.
 * 
 * This class is intended to be overridden and used in conjunction with a
 * CompositeRequestParser: The CompositeRequestParser(or subclass thereof) will
 * hold actual configuration data, and all composed RequestParsers will inherit
 * from this, accessing the configured data on the wrapped instance.
 *
 * For examples, please see {Path,Form,OpenSearch,Composite}RequestParser in
 * this package.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */

public abstract class WrappedRequestParser extends BaseRequestParser {
	
	private BaseRequestParser wrapped = null;

	/**
	 * @param wrapped the delegate to retrieve RequestParser configuration
	 */
	public WrappedRequestParser(BaseRequestParser wrapped) {
		this.wrapped = wrapped;
	}

	public String getEarliestTimestamp() {
		return wrapped.getEarliestTimestamp();
	}
	public String getLatestTimestamp() {
		return wrapped.getLatestTimestamp();
	}
	public int getMaxRecords() {
		return wrapped.getMaxRecords();
	}
}
