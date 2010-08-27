/* CDXFormatToSearchResultAdapter
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
package org.archive.wayback.resourceindex.cdx;


import java.util.logging.Logger;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;
import org.archive.wayback.util.Adapter;

public class CDXFormatToSearchResultAdapter implements Adapter<String,CaptureSearchResult> {
	private static final Logger LOGGER = Logger.getLogger(
			CDXFormatToSearchResultAdapter.class.getName());

	private CDXFormat cdx = null;
	public CDXFormatToSearchResultAdapter(CDXFormat cdx) {
		this.cdx = cdx;
	}

	public CaptureSearchResult adapt(String line) {
		try {
			return cdx.parseResult(line);
		} catch (CDXFormatException e) {
			LOGGER.warning("CDXFormat(" + line + "):"+e.getLocalizedMessage());
		}
		return null;
	}
}
