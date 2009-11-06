/* WaybackConstants
 *
 * $Id$
 *
 * Created on 3:28:47 PM Nov 14, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
