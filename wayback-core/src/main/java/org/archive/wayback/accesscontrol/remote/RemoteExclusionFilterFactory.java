/* RemoteExclusionFilterFactory
 *
 * $Id$
 *
 * Created on 8:23:55 PM Mar 5, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.remote;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RemoteExclusionFilterFactory implements ExclusionFilterFactory {

	private String exclusionUrlPrefix = null;

	private String exclusionUserAgent = null;

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.ExclusionFilterFactory#get()
	 */
	public ObjectFilter<SearchResult> get() {
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
