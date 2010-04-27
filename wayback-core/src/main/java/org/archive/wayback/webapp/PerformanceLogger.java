/* PerformanceLogger
 *
 * $Id$:
 *
 * Created on Mar 19, 2010.
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

package org.archive.wayback.webapp;

import org.apache.log4j.Logger;

/**
 * Brutally simple, barely functional class to allow simple recording of 
 * millisecond level timing within a particular request, enabling rough logging
 * of the time spent in various parts of the handling of a WaybackRequest
 * @author brad
 *
 */
public class PerformanceLogger {
	private static final Logger LOGGER = Logger.getLogger(
			PerformanceLogger.class.getName());

	private static char delim = '\t';
	
	private String type = null;
	private long start = 0;
	private long query = 0;
	private long retrieve = -1;
	private long render = 0;
	/**
	 * Construct a Performance logger with the specified String "type"
	 * @param type the String type to report with the logged output
	 */
	public PerformanceLogger(String type) {
		this.type = type;
		this.start = System.currentTimeMillis();
	}
	/**
	 * record the time when the query associated with this request completed
	 */
	public void queried() {
		this.query = System.currentTimeMillis();
	}
	/**
	 * record the time when the retrieval of a Resource required for this 
	 * request completed, implies a Replay request...
	 */
	public void retrieved() {
		this.retrieve = System.currentTimeMillis();
	}
	/**
	 * record the time when the replayed resource, or the query results were
	 * returned to the client, implies the bulk of the request processing is 
	 * complete.
	 */
	public void rendered() {
		this.render = System.currentTimeMillis();
	}
	/**
	 * Produce a debug message to this classes logger, computing the time
	 * taken to query the index, retrieve the resource (if a replay request)
	 * and render the results to the client. 
	 * @param info String suffix to append to the log message 
	 */
	public void write(String info) {
		StringBuilder sb = new StringBuilder(40);
		sb.append(type).append(delim);
		sb.append(query - start).append(delim);
		if(retrieve == -1) {
			sb.append(render - query).append(delim);
		} else {
			sb.append(retrieve - query).append(delim);
			sb.append(render - retrieve).append(delim);
		}
		sb.append(info);
		LOGGER.debug(sb.toString());
	}
}
