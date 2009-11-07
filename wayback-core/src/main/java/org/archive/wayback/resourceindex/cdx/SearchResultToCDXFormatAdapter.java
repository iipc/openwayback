/* SearchResultToCDXFormatAdapter
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

import java.util.Iterator;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;

public class SearchResultToCDXFormatAdapter implements
		Adapter<CaptureSearchResult, String> {

	private CDXFormat cdx = null;

	public SearchResultToCDXFormatAdapter(CDXFormat cdx) {
		this.cdx = cdx;
	}

	public String adapt(CaptureSearchResult o) {
		return cdx.serializeResult(o);
	}
	public static Iterator<String> adapt(Iterator<CaptureSearchResult> input,
			CDXFormat cdx) {
		SearchResultToCDXFormatAdapter adapter =
			new SearchResultToCDXFormatAdapter(cdx);
		return new AdaptedIterator<CaptureSearchResult,String>(input,adapter);
	}
}
