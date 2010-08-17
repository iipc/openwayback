/* MementoRequestParser
 *
 * $Id$:
 *
 * Created on Aug 16, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
