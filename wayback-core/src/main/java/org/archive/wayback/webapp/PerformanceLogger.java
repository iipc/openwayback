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
	public PerformanceLogger(String type) {
		this.type = type;
		this.start = System.currentTimeMillis();
	}
	public void queried() {
		this.query = System.currentTimeMillis();
	}
	public void retrieved() {
		this.retrieve = System.currentTimeMillis();
	}
	public void rendered() {
		this.render = System.currentTimeMillis();
	}
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
