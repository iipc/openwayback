/* ArchivalUrlSpecialContextResultURIConverter
 *
 * $Id$
 *
 * Created on 12:15:33 PM Feb 12, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
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

import org.archive.wayback.ResultURIConverter;

/**
 * wrapper around an ArchivalUrlResultURIConverter, adding flags after the
 * datespec for a specific context ("js_" for javascript, "cs_" for CSS, etc)
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class ArchivalUrlSpecialContextResultURIConverter 
implements ResultURIConverter {
	

	private String replayURIPrefix = null;
	private String context;
	
	/**
	 * @param converter ArchivalUrlResultURIConverter to wrap
	 * @param context flags indicating the context of URLs created by this 
	 * 				object
	 */
	public ArchivalUrlSpecialContextResultURIConverter(
			ArchivalUrlResultURIConverter converter, String context) {
		replayURIPrefix = converter.getReplayURIPrefix();
		this.context = context;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		String suffix = datespec + context + "/" + url;
		if(replayURIPrefix == null) {
			return suffix;
		} else {
			if(url.startsWith(replayURIPrefix)) {
				return url;
			}
			return replayURIPrefix + suffix;
		}
	}
}
