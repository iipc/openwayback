/* ResultURIConverter
 *
 * $Id$
 *
 * Created on 4:19:21 PM Nov 15, 2005.
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
package org.archive.wayback.proxy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.SearchResult;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RedirectResultURIConverter extends ResultURIConverter {
	
	private static final String DEFAULT_REDIRECT_JSP = "jsp/QueryUI/Redirect.jsp";
    private static final String REDIRECT_PATH_PROPERTY = "proxy.redirectpath";

    private String getRedirectPath() {
		return getConfigOrContextRelative(REDIRECT_PATH_PROPERTY,DEFAULT_REDIRECT_JSP);
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		String res = null;
        try {
			res = getRedirectPath() + "?url=" + URLEncoder.encode(url, "UTF-8") + 
				"&time=" + URLEncoder.encode(datespec, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should not be able to happen -- with hard-coded UTF-8, anyways..
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * @param result 
	 * @return Returns the replayUriPrefix.
	 */
	public String getReplayUriPrefix(final SearchResult result) {
		return "";
	}
}
