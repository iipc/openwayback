/* ArcWarcFilenameFilter
 *
 * $Id$
 *
 * Created on 4:15:56 PM May 29, 2008.
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

import java.io.File;
import java.io.FilenameFilter;

/**
 * FilenameFilter which returns only compressed/uncompressed ARC/WARC files.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArcWarcFilenameFilter implements FilenameFilter {
	private final static String ARC_SUFFIX = ".arc";
	private final static String ARC_GZ_SUFFIX = ".arc.gz";
	private final static String WARC_SUFFIX = ".warc";
	private final static String WARC_GZ_SUFFIX = ".warc.gz";
	
	public boolean accept(File dir, String name) {
		return name.endsWith(ARC_SUFFIX) ||
			name.endsWith(ARC_GZ_SUFFIX) ||
				name.endsWith(WARC_SUFFIX) ||
					name.endsWith(WARC_GZ_SUFFIX);
	}
	
}

