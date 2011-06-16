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
package org.archive.wayback.webapp;

import java.util.logging.Level;
import java.util.logging.Logger;

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
		LOGGER.finer(sb.toString());
	}
	public static void noteElapsed(String message, long elapsed, String note) {
		if(LOGGER.isLoggable(Level.INFO)) {
			StringBuilder sb = new StringBuilder();
			sb.append("WB-PERF\t").append(message).append("\t").append(elapsed);
			if(note != null) {
				sb.append("\t").append(note);
			}
			LOGGER.info(sb.toString());
		}
	}
	
	public static void noteElapsed(String message, long elapsed) {
		noteElapsed(message,elapsed,null);
	}
}
