/* ArchivalUrlRequestParser
 *
 * $Id$
 *
 * Created on 4:11:52 PM Apr 24, 2007.
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
package org.archive.wayback.archivalurl;

import org.archive.wayback.RequestParser;
import org.archive.wayback.archivalurl.requestparser.ArchivalUrlFormRequestParser;
import org.archive.wayback.archivalurl.requestparser.DatelessReplayRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.ReplayRequestParser;
import org.archive.wayback.requestparser.CompositeRequestParser;
import org.archive.wayback.requestparser.FormRequestParser;
import org.archive.wayback.requestparser.OpenSearchRequestParser;

/**
 * CompositeRequestParser that handles Archival Url Replay and Query requests,
 * in addition to "standard" OpenSearch and Form RequestParsers.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArchivalUrlRequestParser extends CompositeRequestParser {
	/**
	 * delimiter character for datespec flags
	 */
	public final static String FLAG_DELIM = "_";
	/**
	 * text/javascript context
	 */
	public final static String JS_CONTEXT = "js"; 
	/**
	 * text/css context
	 */
	public final static String CSS_CONTEXT = "cs";
	/**
	 * image/* context
	 */
	public final static String IMG_CONTEXT = "im";
	/**
	 * raw/identity context
	 */
	public final static String IDENTITY_CONTEXT = "id";
	/**
	 * Charset detection strategy context - should be followed by an integer
	 * indicating which strategy to use 
	 */
	public final static String CHARSET_MODE = "cm";

	protected RequestParser[] getRequestParsers() {
		RequestParser[] theParsers = {
				new ReplayRequestParser(this),
				new PathDatePrefixQueryRequestParser(this),
				new PathDateRangeQueryRequestParser(this),
				new PathPrefixDatePrefixQueryRequestParser(this),
				new PathPrefixDateRangeQueryRequestParser(this),
				new OpenSearchRequestParser(this),
				new ArchivalUrlFormRequestParser(this),
				new DatelessReplayRequestParser(this)
				};
		return theParsers;
	}
}
