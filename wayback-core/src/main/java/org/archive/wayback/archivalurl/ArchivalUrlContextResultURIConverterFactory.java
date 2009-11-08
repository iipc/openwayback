/* ArchivalUrlContextResultURIConverterFactory
 *
 * $Id$:
 *
 * Created on Nov 5, 2009.
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

package org.archive.wayback.archivalurl;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;

/**
 * Factory which creates a context specific ArchivalUrlResultURIConverter,
 * given a base ArchivalUrlResultURIConverter and the flags to add.
 * @author brad
 *
 */
public class ArchivalUrlContextResultURIConverterFactory 
	implements ContextResultURIConverterFactory {
	private ArchivalUrlResultURIConverter converter = null;
	/**
	 * @param converter base ArchivalURLURLConverter to wrap
	 */
	public ArchivalUrlContextResultURIConverterFactory(
			ArchivalUrlResultURIConverter converter) {
		this.converter = converter;
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.html.ContextResultURIConverterFactory#getContextConverter(java.lang.String)
	 */
	public ResultURIConverter getContextConverter(String flags) {
		if(flags == null) {
			return converter;
		}
		return new ArchivalUrlSpecialContextResultURIConverter(converter,flags);
	}

}
