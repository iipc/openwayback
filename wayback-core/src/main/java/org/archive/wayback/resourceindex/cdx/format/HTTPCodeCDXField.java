/* HTTPCodeCDXField
 *
 * $Id$
 *
 * Created on 4:00:41 PM Apr 13, 2009.
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
package org.archive.wayback.resourceindex.cdx.format;

import org.archive.wayback.core.CaptureSearchResult;

public class HTTPCodeCDXField implements CDXField {

	public void apply(String field, CaptureSearchResult result) 
	throws CDXFormatException {
		result.setHttpCode(field);
	}

	public String serialize(CaptureSearchResult result) {
		String r = result.getHttpCode();
		return r == null ? DEFAULT_VALUE : r;
	}
}
