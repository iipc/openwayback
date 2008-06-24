/* ResourceFileSource
 *
 * $Id$
 *
 * Created on 3:49:17 PM May 29, 2008.
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
package org.archive.wayback.resourcestore.resourcefile;

import java.io.IOException;

/**
 * Interface representing the abstract remote or local folder holding ARC/WARC
 * files.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface ResourceFileSource {
	public String getName();
	public String getPrefix();
	public String getBasename(String path);
	public ResourceFileList getResourceFileList() throws IOException;
}
