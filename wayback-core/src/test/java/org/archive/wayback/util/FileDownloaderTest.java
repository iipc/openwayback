/* FileDownloaderTest
 *
 * $Id$
 *
 * Created on 3:46:13 PM Jan 25, 2007.
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

//import java.io.File;
//import java.net.URL;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileDownloaderTest extends TestCase {
	/**
	 * @throws Exception
	 */
	public void testDownload() throws Exception {
//		URL url = new URL("http://homeserver.us.archive.org/~brad/tmp.del.gz");
//		String wantHex = "01051ca0aabef856e9bdcee4ac23f66f"; 
//		File tmp = File.createTempFile("tmp","del");
//		FileDownloader downloader = new FileDownloader();
//		downloader.setDigest(true);
//		downloader.download(url,tmp);
//		assertTrue(tmp.exists());
//		assertEquals(downloader.getLastDigest(),wantHex);
//		assertTrue(tmp.delete());
	}

	/**
	 * @throws Exception
	 */
	public void testDownloadGZ() throws Exception {
//		URL url = new URL("http://homeserver.us.archive.org/~brad/tmp.del.gz");
//		String wantHex = "765dcbfb102670a6e75859599cb38fe4";
//		File tmp = File.createTempFile("tmp","del");
//		FileDownloader downloader = new FileDownloader();
//		downloader.setDigest(true);
//		downloader.downloadGZ(url,tmp);
//		assertTrue(tmp.exists());
//		assertEquals(downloader.getLastDigest(),wantHex);
//		assertTrue(tmp.delete());
	}

}
