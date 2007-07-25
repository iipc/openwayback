/* CachedFile
 *
 * $Id$
 *
 * Created on 3:43:49 PM Jan 25, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

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
