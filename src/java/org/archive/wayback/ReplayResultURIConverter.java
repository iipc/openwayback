/* ReplayURI
 *
 * $Id$
 *
 * Created on 5:20:43 PM Nov 1, 2005.
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

import org.archive.wayback.core.SearchResult;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface ReplayResultURIConverter extends PropertyConfigurable {
	/** Convert a SearchResult into a replayable URL
	 * 
	 * @param result
	 * @return user-viewable String URL that will replay the ResourceResult
	 */
	public String makeReplayURI(final SearchResult result);
	/** create a URL that will drive the client back to the URL argument in 
	 * replay mode, possibly resolving the URL against this SearchResult
	 * @param result
	 * @param url
	 * @return String URL to send client to replay this url
	 */
	public String makeRedirectReplayURI(final SearchResult result, String url);
	/**
	 * @return the URL prefix for the replay service
	 */
	public String getReplayUriPrefix ();
}
