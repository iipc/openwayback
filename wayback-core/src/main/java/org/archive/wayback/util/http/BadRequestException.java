/* BadRequestException
 *
 * $Id$
 *
 * Created on 3:56:12 PM Dec 16, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * SocksProxyCore is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * SocksProxyCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with SocksProxyCore; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util.http;

import java.io.IOException;

public class BadRequestException extends IOException {
	private static final long serialVersionUID = -7123306169949959915L;
	public BadRequestException(String message) {
		super(message);
	}
}
