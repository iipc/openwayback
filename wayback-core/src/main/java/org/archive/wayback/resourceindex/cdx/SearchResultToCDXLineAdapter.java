/* SearchResultToCDXLineAdapter
 *
 * $Id$
 *
 * Created on 3:22:15 PM Jul 26, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.cdx;

import java.util.Iterator;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResultToCDXLineAdapter implements 
Adapter<SearchResult,String>{

	private static int DEFAULT_CAPACITY = 120;
	private final static String DELIMITER = " ";
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public String adapt(SearchResult result) {
		
		StringBuilder sb = new StringBuilder(DEFAULT_CAPACITY);

		sb.append(result.get(WaybackConstants.RESULT_URL_KEY));
		sb.append(DELIMITER);
		sb.append(result.get(WaybackConstants.RESULT_CAPTURE_DATE));
		sb.append(DELIMITER);
		sb.append(result.get(WaybackConstants.RESULT_ORIG_HOST));
		sb.append(DELIMITER);
		sb.append(result.get(WaybackConstants.RESULT_MIME_TYPE));
		sb.append(DELIMITER);
		sb.append(result.get(WaybackConstants.RESULT_HTTP_CODE));
		sb.append(DELIMITER);
		sb.append(result.get(WaybackConstants.RESULT_MD5_DIGEST));
		sb.append(DELIMITER);
		sb.append(result.get(WaybackConstants.RESULT_REDIRECT_URL));
		sb.append(DELIMITER);
		sb.append(result.get(WaybackConstants.RESULT_OFFSET));
		sb.append(DELIMITER);
		sb.append(result.get(WaybackConstants.RESULT_ARC_FILE));
			
		return sb.toString();
	}

	public static Iterator<String> adapt(Iterator<SearchResult> input) {
		return new AdaptedIterator<SearchResult,String>(input,
				new SearchResultToCDXLineAdapter());
	}
}
