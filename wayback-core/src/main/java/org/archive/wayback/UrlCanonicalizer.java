/* UrlCanonicalizer
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

import org.apache.commons.httpclient.URIException;

/**
 * Interface for implementations that transform an input String URL into a
 * canonical form, suitable for lookups in a ResourceIndex. URLs should be sent
 * through the same canonicalizer they will be searched using, before being 
 * inserted into a ResourceIndex.
 * @author brad
 *
 */
public interface UrlCanonicalizer {
	/**
	 * @param url String representation of an URL, in as original, and 
	 * 		unchanged form as possible.
	 * @return a lookup key appropriate for searching within a ResourceIndex.
	 * @throws URIException if the input url String is not a valid URL.
	 */
	public String urlStringToKey(String url) throws URIException;
}
