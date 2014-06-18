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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.HttpHeaderOperation;

public class ArcResource extends Resource {

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
	ArchiveReader arcReader = null;
	/**
	 * flag to indicate if the ARCRecord skipHTTPHeader() has been called
	 */
	boolean parsedHeader = false;
	/**
	 * Expandable property bag for holding metadata associated with this 
	 * resource
	 */
	Hashtable<String,String> metaData = new Hashtable<String,String>();
	
	/**
	 * Constructor
	 * 
	 * @param rec
	 * @param reader 
	 */
	public ArcResource(final ARCRecord rec,final ArchiveReader reader) {
		super();
		arcRecord = rec;
		arcReader = reader;
		setInputStream(rec);
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
					if(name.toUpperCase().contains(
							HttpHeaderOperation.HTTP_TRANSFER_ENC_HEADER)) {
						if(value.toUpperCase().contains(
								HttpHeaderOperation.HTTP_CHUNKED_ENCODING_HEADER)) {
							setChunkedEncoding();
						}
					}
				}
			}

			// copy all ARC record header fields to metaData, prefixing with 
			// ARC_META_PREFIX
			Map<String,Object> headerMetaMap = arcRecord.getMetaData().getHeaderFields();
			Set<String> keys = headerMetaMap.keySet();
			Iterator<String> itr = keys.iterator();
			while(itr.hasNext()) {
				String metaKey = itr.next();
				Object value = headerMetaMap.get(metaKey);
				String metaValue = "";
				if(value != null) {
					metaValue = value.toString();
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
	 * @see org.archive.io.arc.ARCRecord#close()
	 */
	public void close() throws IOException {
		arcRecord.close();
		if(arcReader != null) {
			arcReader.close();
		}
	}
	
	/**
	 * @return byte length claimed in ARC record metadata line.
	 */
	public long getRecordLength() {
		return arcRecord.getMetaData().getLength();
	}
}
