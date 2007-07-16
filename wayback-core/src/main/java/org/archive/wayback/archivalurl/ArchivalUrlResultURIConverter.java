/* ResultURIConverter
 *
 * $Id$
 *
 * Created on 5:24:36 PM Nov 1, 2005.
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
package org.archive.wayback.archivalurl;

import java.util.Properties;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.PropertyConfiguration;
import org.archive.wayback.exception.ConfigurationException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArchivalUrlResultURIConverter implements ResultURIConverter {
	/**
	 * configuration name for URL prefix of replay server
	 */
	private final static String REPLAY_URI_PREFIX_PROPERTY = "replayuriprefix";
	private String replayURIPrefix = null;
	public void init(Properties p) throws ConfigurationException {
		PropertyConfiguration pc = new PropertyConfiguration(p);
		replayURIPrefix = pc.getString(REPLAY_URI_PREFIX_PROPERTY);
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		if(replayURIPrefix == null) {
			return datespec + "/" + url;
		} else {
			return replayURIPrefix + datespec + "/" + url;
		}
	}

	/**
	 * @param replayURIPrefix the replayURIPrefix to set
	 */
	public void setReplayURIPrefix(String replayURIPrefix) {
		this.replayURIPrefix = replayURIPrefix;
	}
}
