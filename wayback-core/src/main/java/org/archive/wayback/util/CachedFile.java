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
package org.archive.wayback.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.util.flatfile.FlatFile;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CachedFile extends FlatFile {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private URL sourceUrl;
	private File targetFile;
	private long checkIntervalMS = 0;
	private long nextCheckMS;
	
	/**
	 * @param targetFile
	 * @param sourceUrl
	 * @param checkIntervalMS
	 */
	public CachedFile(File targetFile, URL sourceUrl, long checkIntervalMS) {
		super(targetFile.getAbsolutePath());
		this.targetFile = targetFile;
		this.sourceUrl = sourceUrl;
		this.checkIntervalMS = checkIntervalMS;
		this.nextCheckMS = 0;
	}

	private void refreshFromSource() throws IOException {
		File tmpFile = new File(targetFile.getParentFile(),
				targetFile.getName() + ".TMP");
		
		InputStream is = sourceUrl.openStream();
		OutputStream fos = new BufferedOutputStream(
				new FileOutputStream(tmpFile));
		int BUF_SIZE = 4096;
		byte[] buffer = new byte[BUF_SIZE];
		for (int r = -1; (r = is.read(buffer, 0, BUF_SIZE)) != -1;) {
			fos.write(buffer, 0, r);
		}
		fos.flush();
		fos.close();
		tmpFile.renameTo(targetFile);
	}
	
	/**
	 * @return Iterator of lines in File
	 * @throws IOException
	 */
	public CloseableIterator<String> getSequentialIterator() throws IOException {
		long nowMS = System.currentTimeMillis();
		if(nowMS > nextCheckMS) {
			refreshFromSource();
			nextCheckMS = nowMS + checkIntervalMS;
		}
//		FlatFile ff = new FlatFile(targetFile.getAbsolutePath());
		return super.getSequentialIterator();
	}
}
