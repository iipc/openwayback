/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
	public final static String ARC_SUFFIX = ".arc";
	public final static String ARC_GZ_SUFFIX = ".arc.gz";
	public final static String WARC_SUFFIX = ".warc";
	public final static String WARC_GZ_SUFFIX = ".warc.gz";
	public final static String OPEN_SUFFIX = ".open";
	
	public boolean accept(File dir, String name) {
		return name.endsWith(ARC_SUFFIX) ||
			name.endsWith(ARC_GZ_SUFFIX) ||
				name.endsWith(WARC_SUFFIX) ||
					name.endsWith(WARC_GZ_SUFFIX);
	}
	
}

