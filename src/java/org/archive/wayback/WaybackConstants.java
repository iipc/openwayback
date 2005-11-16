/* WaybackConstants
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

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface WaybackConstants {
	
	/**
	 * Request: filter results before this 14-digit timestamp 
	 */
	public static final String REQUEST_START_DATE = "startdate";

	/**
	 * Request: filter results after this 14-digit timestamp 
	 */
	public static final String REQUEST_END_DATE = "enddate";
	
	/**
	 * Request: (replay) find closest result to this 14-digit timestamp 
	 */
	public static final String REQUEST_EXACT_DATE = "exactdate";
	
	/**
	 * Request: URL or URL prefix requested 
	 */
	public static final String REQUEST_URL = "url";
	
	/**
	 * Request: URL of referrer, if supplied, or "" if not 
	 */
	public static final String REQUEST_REFERER_URL = "refererurl";
	
	/**
	 * Request: defines type - urlquery, urlprefixquery, or replay 
	 */
	public static final String REQUEST_TYPE = "type";

	/**
	 * Request: urlquery type request 
	 */
	public static final String REQUEST_URL_QUERY = "urlquery";

	/**
	 * Request: urlprefixquery type request 
	 */
	public static final String REQUEST_URL_PREFIX_QUERY = "urlprefixquery";

	/**
	 * Request: replay type request 
	 */
	public static final String REQUEST_REPLAY_QUERY = "replay";

	/**
	 * Results: int first record of all matching returned, 1-based 
	 */
	public static final String RESULTS_FIRST_RECORD = "firstrecord";

	/**
	 * Results: int first page of all matching pages to return, 1-based
	 */
	public static final String RESULTS_FIRST_PAGE = "firstpage";
	
	/**
	 * Results: boolean: "true"|"false" if there are more records matching
	 * than those returned in the currect SearchResults
	 */
	public static final String RESULTS_HAS_MORE = "hasmore";
	

	/**
	 * Result: URL of captured document 
	 */
	public static final String RESULT_URL = "url";
	
	/**
	 * Result: 14-digit timestamp when document was captured 
	 */
	public static final String RESULT_CAPTURE_DATE = "capturedate";

	/**
	 * Result: basename of ARC file containing this document.
	 */
	public static final String RESULT_ARC_FILE = "arcfile";

	/**
	 * Result: compressed byte offset within ARC file where this document's
	 * gzip envelope begins. 
	 */
	public static final String RESULT_OFFSET = "compressedoffset";
	
	/**
	 * Result: original exact host from which this document was captured.
	 */
	public static final String RESULT_ORIG_HOST = "originalhost";
	
	/**
	 * Result: best-guess at mime-type of this document.
	 */
	public static final String RESULT_MIME_TYPE = "mimetype";

	/**
	 * Result: 3-digit integer HTTP response code. may be '0' in some
	 * fringe conditions, old ARCs, bug in crawler, etc.
	 */
	public static final String RESULT_HTTP_CODE = "httpresponsecode";

	/**
	 * Result: all or part of the 32-digit hexadecimal MD5 digest of this 
	 * document
	 */
	public static final String RESULT_MD5_DIGEST= "md5digest";

	/**
	 * Result: URL that this document redirected to, or '-' if it does
	 * not redirect
	 */
	public static final String RESULT_REDIRECT_URL = "redirecturl";
	
}
