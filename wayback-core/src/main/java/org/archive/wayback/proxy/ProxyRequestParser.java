/* ProxyRequestParser
 *
 * $Id$
 *
 * Created on 3:42:13 PM Apr 26, 2007.
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
package org.archive.wayback.proxy;

import java.util.List;

import org.archive.wayback.RequestParser;
import org.archive.wayback.requestparser.CompositeRequestParser;
import org.archive.wayback.requestparser.FormRequestParser;
import org.archive.wayback.requestparser.OpenSearchRequestParser;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ProxyRequestParser extends CompositeRequestParser {
	private ProxyReplayRequestParser prrp = new ProxyReplayRequestParser();
	protected RequestParser[] getRequestParsers() {
		prrp.init();
		RequestParser[] theParsers = {
				prrp,
				new OpenSearchRequestParser(),
				new FormRequestParser() 
				};
		return theParsers;
	}
	public List<String> getLocalhostNames() {
		return prrp.getLocalhostNames();
	}
	public void setLocalhostNames(List<String> localhostNames) {
		prrp.setLocalhostNames(localhostNames);
	}
}
