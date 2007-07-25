/* Resource
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCRecord;

/**
 * Slightly more than an ARCRecord. This class is designed to be an abstraction
 * to allow the Wayback to operator with non-ARC file format resources. Probably
 * the interface required will end up looking very much like ARCRecord, but can
 * be reimplemented to handle new ARC formats or non-ARC formats.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class Resource extends InputStream {
	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = Logger.getLogger(Resource.class
			.getName());

	/**
	 * String prefix for ARC file related metadata namespace of keys within 
	 * metaData Properties bag.
	 */
	private static String ARC_META_PREFIX = "arcmeta.";
	/**
	 * String prefix for HTTP Header related metadata namespace of keys within 
	 * metaData Properties bag.
	 */
	private static String HTTP_HEADER_PREFIX = "httpheader.";
	/**
	 * object for ARCRecord
	 */
	ARCRecord arcRecord = null;
	/**
	 * object for ARCReader -- need to hold on to this in order to call close()
	 * to release filehandle after completing access to this record. optional
	 */
	ARCReader arcReader = null;
	/**
	 * flag to indicate if the ARCRecord skipHTTPHeader() has been called
	 */
	boolean parsedHeader = false;
	/**
	 * Expandable property bag for holding metadata associated with this 
	 * resource
	 */
	Hashtable<String,String> metaData = new Hashtable<String,String>();
	
	private BufferedInputStream bis;
	
	/**
	 * Constructor
	 * 
	 * @param rec
	 * @param reader 
	 */
	public Resource(final ARCRecord rec,final ARCReader reader) {
		super();
		arcRecord = rec;
		arcReader = reader;
		bis = new BufferedInputStream(rec);
	}

	/** parse the headers on the underlying ARC record, and extract all 
	 * @throws IOException
	 */
	public void parseHeaders () throws IOException {
		if(!parsedHeader) {
			arcRecord.skipHttpHeader();
			// copy all HTTP headers to metaData, prefixing with 
			// HTTP_HEADER_PREFIX
			Header[] headers = arcRecord.getHttpHeaders();
			if (headers != null) {
				for (int i = 0; i < headers.length; i++) {
					String value = headers[i].getValue();
					String name = headers[i].getName();
					metaData.put(HTTP_HEADER_PREFIX + name,value);
				}
			}

			// copy all ARC record header fields to metaData, prefixing with 
			// ARC_META_PREFIX
			@SuppressWarnings("unchecked")
			Map<String,String> headerMetaMap = arcRecord.getMetaData().getHeaderFields();
			Set<String> keys = headerMetaMap.keySet();
			Iterator<String> itr = keys.iterator();
			while(itr.hasNext()) {
				String metaKey = itr.next();
				String metaValue = headerMetaMap.get(metaKey);
				if(metaValue == null) {
					metaValue = "";
				}
				metaData.put(ARC_META_PREFIX + metaKey,metaValue);
			}
		
			parsedHeader = true;			
		}
	}

	/**
	 * @param prefix
	 * @return a Properties of all elements in metaData starting with 'prefix'.
	 *         keys in the returned Properties have 'prefix' removed.
	 */
	public Map<String,String> filterMeta(String prefix) {
		HashMap<String,String> matching = new HashMap<String,String>();
		for (Enumeration<String> e = metaData.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			if (key.startsWith(prefix)) {
				String finalKey = key.substring(prefix.length());
				String value = metaData.get(key);
				matching.put(finalKey, value);
			}
		}
		return matching;
	}
	
	/**
	 * @return a Properties containing all HTTP header fields for this record
	 */
	public Map<String,String> getHttpHeaders() {
		return filterMeta(HTTP_HEADER_PREFIX);
	}

	/**
	 * @return a Properties containing all ARC Meta fields for this record
	 */
	public Map<String,String> getARCMetadata() {
		return filterMeta(ARC_META_PREFIX);
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.archive.io.arc.ARCRecord#getStatusCode()
	 * @return int HTTP status code returned with this document. 
	 */
	public int getStatusCode() {
		return arcRecord.getStatusCode();
	}

	/**
	 * @return the ARCRecord underlying this Resource.
	 */
	public ArchiveRecord getArcRecord() {
		return arcRecord;
	}

	/* (non-Javadoc)
	 * @see org.archive.io.arc.ARCRecord#read()
	 */
	public int read() throws IOException {
		return bis.read();
	}

	/* (non-Javadoc)
	 * @see org.archive.io.arc.ARCRecord#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		return bis.read(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		return bis.read(b);
	}

	/* (non-Javadoc)
	 * @see org.archive.io.arc.ARCRecord#skip(long)
	 */
	public long skip(long arg0) throws IOException {
		return bis.skip(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.BufferedInputStream#available()
	 */
	public int available() throws IOException {
		return bis.available();
	}

	/* (non-Javadoc)
	 * @see java.io.BufferedInputStream#mark(int)
	 */
	public void mark(int readlimit) {
		bis.mark(readlimit);
	}

	/* (non-Javadoc)
	 * @see java.io.BufferedInputStream#markSupported()
	 */
	public boolean markSupported() {
		return bis.markSupported();
	}

	/* (non-Javadoc)
	 * @see java.io.BufferedInputStream#reset()
	 */
	public void reset() throws IOException {
		bis.reset();
	}

	/* (non-Javadoc)
	 * @see org.archive.io.arc.ARCRecord#close()
	 */
	public void close() throws IOException {
		//LOGGER.info("About to close..("+arcReader+")");
		arcRecord.close();
		if(arcReader != null) {
			arcReader.close();
			LOGGER.info("closed..("+arcReader+")");
		}
	}
	
	/**
	 * @return byte length claimed in ARC record metadata line.
	 */
	public long getRecordLength() {
		return arcRecord.getMetaData().getLength();
	}
}
