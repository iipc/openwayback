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
