/* IdentityUrlCanonicalizer
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
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
package org.archive.wayback.util.url;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;

/**
 * Identity UrlCanonicalizer implementation, passing through urls as-is.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class IdentityUrlCanonicalizer implements UrlCanonicalizer {

	/* (non-Javadoc)
	 * @see org.archive.wayback.UrlCanonicalizer#urlStringToKey(java.lang.String)
	 */
	public String urlStringToKey(String url) throws URIException {
		return url;
	}
}
