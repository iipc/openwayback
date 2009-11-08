/* ResultURIConverter
 *
 * $Id$
 *
 * Created on 5:20:43 PM Nov 1, 2005.
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
package org.archive.wayback;

/**
 * Interface for implementations that convert a string datespec and URL into
 * an absolute URL that will replay the specified URL at the specified date.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public interface ResultURIConverter {
	/**
	 * return an absolute URL that will replay URL url at time datespec.
	 * 
	 * @param datespec 14-digit timestamp for the desired Resource
	 * @param url for the desired Resource
	 * @return absolute replay URL
	 */
	public String makeReplayURI(final String datespec, final String url);
}
