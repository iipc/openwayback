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
package org.archive.wayback;


/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface WaybackConstants {
	
	/**
	 * prefixes of HTTP protocol URL.. 
	 */
	public static final String HTTP_URL_PREFIX = "http://";
	public static final String HTTPS_URL_PREFIX = "https://";
        public static final String HTTPS_URL_PREFIX_FOR_REWRITE_DECISION = "https";

	/**
	 * default HTTP port: 
	 */
	public static final String HTTP_DEFAULT_PORT = "80";

	/**
	 * prefixes of DNS Record URLs.. 
	 */
    public static final String DNS_URL_PREFIX = "dns:";
	
	/**
	 * HTTP Header for redirection URL
	 */
	public final static String LOCATION_HTTP_HEADER = "Location";

	/**
	 * HTTP Header for robot instructions. See http://noarchive.net/xrobots/
	 */
	public final static String X_ROBOTS_HTTP_HEADER = "X-Robots-Tag";
}
