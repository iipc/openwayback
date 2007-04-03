/* RobotExclusionFilterFactory
 *
 * $Id$
 *
 * Created on 3:49:53 PM Mar 14, 2007.
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
package org.archive.wayback.accesscontrol.robotstxt;

import java.util.Properties;

import org.archive.wayback.core.PropertyConfiguration;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourceindex.ExclusionFilterFactory;
import org.archive.wayback.resourceindex.SearchResultFilter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RobotExclusionFilterFactory implements ExclusionFilterFactory {

	private final static String ROBOT_USER_AGENT = "robotexclusion.useragent";
	private final static String ROBOT_CACHE_AGE = "robotexclusion.cacheagems";


	private LiveWebCache webCache = null;
	private String userAgent = null;
	private long maxCacheMS = 0;

	/* (non-Javadoc)
	 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
		PropertyConfiguration pc = new PropertyConfiguration(p);
		userAgent = pc.getString(ROBOT_USER_AGENT);
		maxCacheMS = pc.getLong(ROBOT_CACHE_AGE);
		webCache = new LiveWebCache();
		webCache.init(p);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.ExclusionFilterFactory#get()
	 */
	public SearchResultFilter get() {
		return new RobotExclusionFilter(webCache,userAgent,maxCacheMS);
	}
}
