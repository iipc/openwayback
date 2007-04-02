/* BetterRequestException
 *
 * $Id$
 *
 * Created on 6:42:01 PM Oct 31, 2005.
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
package org.archive.wayback.exception;

/**
 * Exception class for queries which can be better expressed as another URL, or
 * should, for one reason or another, be requested at a different URL. Likely
 * cause would be to redirect the client so the Browser location reflects the
 * exact request being served. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BetterRequestException extends WaybackException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "betterRequest";
	private String betterURI;

	/**
	 * Constructor
	 * @param betterURI 
	 * 
	 */
	public BetterRequestException(String betterURI) {
		super("Better URI for query");
		this.betterURI = betterURI;
		id = ID;
	}

	/**
	 * @return Returns the betterURI.
	 */
	public String getBetterURI() {
		return betterURI;
	}
}
