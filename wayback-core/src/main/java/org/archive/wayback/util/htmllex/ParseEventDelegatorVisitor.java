/* ParseEventDelegatorVisitor
 *
 * $Id$
 *
 * Created on 12:36:59 PM Nov 5, 2009.
 *
 * Copyright (C) 2008 Internet Archive.
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
package org.archive.wayback.util.htmllex;


/**
 * 
 * Common interface to decouple application-specific handlers from the
 * ParseEventDelegator object: Any object interested in registering for specific
 * low-level events can implement this interface, and can be added to the 
 * ParseEventDelegator parserVisitors list, and it will be given an opportunity
 * to register with the ParseEventDelegator for specific events it is 
 * interested in.
 * 
 * @author brad
 *
 */
public interface ParseEventDelegatorVisitor {
	public void visit(ParseEventDelegator rules);
}
