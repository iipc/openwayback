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
	 * prefixes of HTTP protocol URL.. 
	 */
	public static final String HTTP_URL_PREFIX = "http://";

	/**
	 * default HTTP port: 
	 */
	public static final String HTTP_DEFAULT_PORT = "80";

	/**
	 * prefixes of DNS Record URLs.. 
	 */
    public static final String DNS_URL_PREFIX = "dns:";
	
	/**
	 * Request: (query) filter results to those prefixed with this (possibly 
	 * partial) 14-digit timestamp 
	 */
	public static final String REQUEST_DATE = "date";

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
     * Request: Original URL or URL prefix requested.
     * This version differs from @{link {@link #REQUEST_URL} in that its
     * the URL before it was passed via the UURIFactory cleanup.
     */
    public static final String REQUEST_URL_CLEANED = "cleanedurl";
	
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
	 * Request: closest type request 
	 */
	public static final String REQUEST_CLOSEST_QUERY = "urlclosestquery";

	/**
	 * Request: resolution of results to be displayed: (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION = "resolution";
	
	/**
	 * Request: replay actual document or metadata for document: "yes" means 
	 * replay metadata only, not the actual document: (TimeLine mode)
	 */
	public static final String REQUEST_META_MODE = "metamode";
	
	/**
	 * Request: hour resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_HOURS = "hours";
	
	/**
	 * Request: day resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_DAYS = "days";
	
	/**
	 * Request: month resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_MONTHS = "months";
	
	/**
	 * Request: year resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_YEARS = "years";

	/**
	 * Request: auto resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_AUTO = "auto";
	
	/**
	 * Request: Remote Address, string IP address: "127.0.0.1" 
	 */
	public static final String REQUEST_REMOTE_ADDRESS = "remoteaddress";

	/**
	 * Request: Wayback Hostname: the string "Host:" HTTP header  
	 */
	public static final String REQUEST_WAYBACK_HOSTNAME = "waybackhostname";
	
	/**
	 * Request: Wayback Port: the port the remote user connected to for this
	 * request.  
	 */
	public static final String REQUEST_WAYBACK_PORT = "waybackport";
	
	/**
	 * Request: Wayback Context: the string context used in the request,
	 * if applicable.
	 */
	public static final String REQUEST_WAYBACK_CONTEXT = "waybackcontext";

	/**
	 * Request: Authorization Type: "BASIC", "SSL", or "" if none.
	 */
	public static final String REQUEST_AUTH_TYPE = "requestauthtype";

	/**
	 * Request: Remote User or "" if the request did not contain auth info.
	 */
	public static final String REQUEST_REMOTE_USER = "requestremoteuser";

	
	/**
	 * Results: int total number of records matching, not all necc. returned.
	 */
	public static final String RESULTS_NUM_RESULTS = "numresults";
	
	/**
	 * Results: int first record of all matching returned, 1-based 
	 */
	public static final String RESULTS_FIRST_RETURNED = "firstreturned";

	/**
	 * Results: int total number of records *returned* in results 
	 */
	public static final String RESULTS_NUM_RETURNED = "numreturned";
	
	/**
	 * Results: int number of results requested
	 */
	public static final String RESULTS_REQUESTED = "resultsrequested";
	

	/**
	 * Result: as close as possible to original URL of captured document given
	 * informatio accessible only within the index.
	 * 
	 * This may not always yield the exact URL requested: some canonicalization
	 * operations are irreversible.
	 *      Example: lowercasing of GET URI encoded arguments
	 *        http://foo.com/q?a=%3F => foo.com/q?a=%3f
	 *      Example: rearrangement of GET URI encoded arguments
	 *        http://foo.com/q?b=1&a=1 => foo.com/q?a=1&b=1
	 *      Example: removal of sessionID information from request path
	 *        http://foo.com/SESSION_A2KSM2/i.htm => foo.com/i.htm
	 * 
	 */
	public static final String RESULT_URL = "url";

	/**
	 * Result: canonicalized(lookup key) form of URL of captured document 
	 */
	public static final String RESULT_URL_KEY = "urlkey";
	
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
	 * Result: compressed byte offset within ARC file where this document's
	 * gzip envelope Ends. 
	 */
	public static final String RESULT_END_OFFSET = "compressedendoffset";
	
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
	
	/**
	 * Name of configuration in web.xml for maximum number of results to return
	 * in index searches.
	 */
	public static final String MAX_RESULTS_CONFIG_NAME = "maxresults";

	/**
	 * Name of configuration in web.xml for default number of results to show
	 * on each page
	 */
	public static final String RESULTS_PER_PAGE_CONFIG_NAME = "resultsperpage";
	
}
