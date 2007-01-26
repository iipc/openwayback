/* FileDownloader
 *
 * $Id$
 *
 * Created on 3:45:22 PM Jan 25, 2007.
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
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Hex;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileDownloader {
	private boolean digest = false;
	private String lastDigest;
	/**
	 * @return Returns the lastDigest.
	 */
	public String getLastDigest() {
		return lastDigest;
	}
	
	/**
	 * @param digest The digest to set.
	 */
	public void setDigest(boolean digest) {
		this.digest = digest;
	}

	private void download(URL url, File target, boolean gz) throws IOException,
		NoSuchAlgorithmException {

		OutputStream os;
		MessageDigest digester = null;
		InputStream is;
		if(gz) {
			is = new GZIPInputStream(url.openStream());
		} else {
			is = url.openStream();
		}
		
		FileOutputStream fos = new FileOutputStream(target);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		if(digest) {
			digester = MessageDigest.getInstance("MD5");
			os = new DigestOutputStream(bos,digester);
		} else {
			os = bos;
		}
		int BUF_SIZE = 4096;
		byte[] buffer = new byte[BUF_SIZE];
		for (int r = -1; (r = is.read(buffer, 0, BUF_SIZE)) != -1;) {
			os.write(buffer, 0, r);
		}
		is.close();
		os.flush();
		os.close();
		if(digest) {
			this.lastDigest = new String(Hex.encodeHex(digester.digest()));
		}
	}
	
	/**
	 * @param url URL to download
	 * @param target local File where uncompressed content of URL is stored
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void download(URL url,File target) throws IOException, NoSuchAlgorithmException {
		download(url,target,false);
	}

	/**
	 * @param url URL to download
	 * @param target local File where uncompressed content of URL is stored
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void downloadGZ(URL url,File target) throws IOException, NoSuchAlgorithmException {
		download(url,target,true);
	}
}
