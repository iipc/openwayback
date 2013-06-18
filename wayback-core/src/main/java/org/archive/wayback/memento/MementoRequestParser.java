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
package org.archive.wayback.memento;

import org.archive.wayback.RequestParser;
import org.archive.wayback.archivalurl.ArchivalUrlRequestParser;
import org.archive.wayback.archivalurl.requestparser.ArchivalUrlFormRequestParser;
import org.archive.wayback.archivalurl.requestparser.DatelessReplayRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.ReplayRequestParser;
import org.archive.wayback.requestparser.OpenSearchRequestParser;

/**
 * @author brad
 *
 */
public class MementoRequestParser extends ArchivalUrlRequestParser {
	protected RequestParser[] getRequestParsers() {
		// all the usual ArchivalURL RequestParsers, plus the memento-specific 
		// ones:
		RequestParser[] theParsers = {
				new ReplayRequestParser(this),
				new TimeGateRequestParser(this),
				new TimeMapRequestParser(this),
				new TimeBundleRequestParser(this),
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
