/* WrappedRequestParser
 *
 * $Id$
 *
 * Created on 11:53:19 AM Apr 9, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
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
package org.archive.wayback.requestparser;

/**
 * Abstract subclass of BaseRequestParser, which allows retrieving
 * configured maxRecords, and earliest and latest timestamp config from an
 * delegate instance.
 * 
 * This class is intended to be overridden and used in conjunction with a
 * CompositeRequestParser: The CompositeRequestParser(or subclass thereof) will
 * hold actual configuration data, and all composed RequestParsers will inherit
 * from this, accessing the configured data on the wrapped instance.
 *
 * For examples, please see {Path,Form,OpenSearch,Composite}RequestParser in
 * this package.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */

public abstract class WrappedRequestParser extends BaseRequestParser {
	
	private BaseRequestParser wrapped = null;

	/**
	 * @param wrapped the delegate to retrieve RequestParser configuration
	 */
	public WrappedRequestParser(BaseRequestParser wrapped) {
		this.wrapped = wrapped;
	}

	public String getEarliestTimestamp() {
		return wrapped.getEarliestTimestamp();
	}
	public String getLatestTimestamp() {
		return wrapped.getLatestTimestamp();
	}
	public int getMaxRecords() {
		return wrapped.getMaxRecords();
	}
}
