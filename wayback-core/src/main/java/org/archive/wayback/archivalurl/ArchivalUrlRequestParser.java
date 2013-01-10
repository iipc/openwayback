/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
	 * frame-wrapper context
	 */
	public final static String FRAME_WRAPPED_CONTEXT = "fw";
	/**
	 * iframe-wrapped context
	 */
	public final static String IFRAME_WRAPPED_CONTEXT = "if";
	/**
	 * object/embed wrapped context
	 */
	public final static String OBJECT_EMBED_WRAPPED_CONTEXT = "oe";	
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
