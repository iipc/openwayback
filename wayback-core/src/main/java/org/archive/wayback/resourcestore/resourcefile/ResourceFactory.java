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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.webapp.PerformanceLogger;

/**
 * Static factory class for constructing ARC/WARC Resources from 
 * File/URL + offset.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFactory {
	private static final Logger LOGGER = 
		Logger.getLogger(ResourceFactory.class.getName());
	
	//TODO: better way to set default settings?
	private static TimeoutArchiveReaderFactory defaultTimeoutReader = new TimeoutArchiveReaderFactory();

	static public class DefaultTimeoutArchiveReaderFactory extends TimeoutArchiveReaderFactory
	{
		public DefaultTimeoutArchiveReaderFactory()
		{
			super();
			defaultTimeoutReader = this;
		}
		
		public DefaultTimeoutArchiveReaderFactory(int connectTimeout, int readTimeout) {
			super(connectTimeout, readTimeout);
			defaultTimeoutReader = this;			
		}
		
		public DefaultTimeoutArchiveReaderFactory(int connectTimeout) {
			super(connectTimeout);
			defaultTimeoutReader = this;
		}
	}
	
	
	public static Resource getResource(String urlOrPath, long offset)
	throws IOException, ResourceNotAvailableException {
		LOGGER.info("Fetching: " + urlOrPath + " : " + offset);
		try {
			if(urlOrPath.startsWith("http://")) {
				return getResource(new URL(urlOrPath), offset);
            } else if(urlOrPath.startsWith("hdfs://")) {           	
                try {
                  return getResource(new URI(urlOrPath), offset);
                  
                } catch ( java.net.URISyntaxException use ) {
                  // Stupid Java, the URISyntaxException is not a sub-type of IOException,
                  // unlike MalformedURLException.
                  throw new IOException( use );
                }
			} else {
				// assume local path:
				return getResource(new File(urlOrPath), offset);
			}
		} catch(ResourceNotAvailableException e) {
			LOGGER.warning("ResourceNotAvailable for " + urlOrPath + " " + e.getMessage());
			throw e;
		} catch(IOException e) {
			LOGGER.warning("ResourceNotAvailable for " + urlOrPath + " " + e.getMessage());
			throw e;
		}
	}
	
	protected static FileSystem hdfsSys = null;

  public static Resource getResource( URI uri, long offset)
    throws IOException, ResourceNotAvailableException, URISyntaxException {
    
    Resource r = null;
    
    // FIXME: Put this into static initialization?  or require
    //        explicit init during startup?  Or just create it each
    //        time?
    // 
    
    // Attempt at fix: Only initializing file system once    
    if (hdfsSys == null)
    {
        Configuration conf = new Configuration();

        // Assume that the URL is a fully-qualified HDFS url, like:
        //   hdfs://namenode:6100/collections/foo/some.arc.gz
        // create fs with just the default URL
        
        URI defaultURI = new URI(uri.getScheme() + "://" + uri.getHost() + ":"+ uri.getPort() + "/");
        hdfsSys = FileSystem.get(defaultURI, conf);
    }
        
    Path path = new Path( uri.getPath() );

    FSDataInputStream is = hdfsSys.open( path );
    is.seek( offset );

    if (isArc(path.getName()))
      {
        ArchiveReader reader = ARCReaderFactory.get(path.getName(), is, false);
        r = ARCArchiveRecordToResource(reader.get(), reader);
      }
    else if (isWarc(path.getName()))
      {
        ArchiveReader reader = WARCReaderFactory.get(path.getName(), is, false);
        r = WARCArchiveRecordToResource(reader.get(), reader);
      } 
    else 
      {
    	is.close();
        throw new ResourceNotAvailableException("Unknown extension");
      }
    
    return r;
  }

	public static Resource getResource(File file, long offset)
			throws IOException, ResourceNotAvailableException {

		Resource r = null;
		String name = file.getName();
		if (name.endsWith(ArcWarcFilenameFilter.OPEN_SUFFIX)) {
			name = name.substring(0, name.length()
					- ArcWarcFilenameFilter.OPEN_SUFFIX.length());
		}
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		raf.seek(offset);
		InputStream is = new FileInputStream(raf.getFD());
		String fPath = file.getAbsolutePath();
		if (isArc(name)) {
			ArchiveReader reader = ARCReaderFactory.get(fPath, is, false);
			r = ARCArchiveRecordToResource(reader.get(), reader);

		} else if (isWarc(name)) {

			ArchiveReader reader = WARCReaderFactory.get(fPath, is, false);
			r = WARCArchiveRecordToResource(reader.get(), reader);

		} else {
			throw new ResourceNotAvailableException("Unknown extension");
		}

		return r;
	}
	public static Resource getResource(URL url, long offset)
	throws IOException, ResourceNotAvailableException {
		
		Resource r = null;
		long start = System.currentTimeMillis();
		TimeoutArchiveReaderFactory tarf = defaultTimeoutReader;
		ArchiveReader reader = tarf.getArchiveReader(url,offset);
		if(reader instanceof ARCReader) {
			ARCReader areader = (ARCReader) reader;
			r = ARCArchiveRecordToResource(areader.get(),areader);
		
		} else if(reader instanceof WARCReader) {
			WARCReader wreader = (WARCReader) reader;
			r = WARCArchiveRecordToResource(wreader.get(),wreader);
			
		} else {
			throw new ResourceNotAvailableException("Unknown ArchiveReader");
		}
		long elapsed = System.currentTimeMillis() - start;
		PerformanceLogger.noteElapsed("Http11Resource", elapsed, url.toExternalForm());
		return r;
	}
	
	
	private static boolean isArc(final String name) {

		return (name.endsWith(ArcWarcFilenameFilter.ARC_SUFFIX)
				|| name.endsWith(ArcWarcFilenameFilter.ARC_GZ_SUFFIX));
	}

	private static boolean isWarc(final String name) {

		return (name.endsWith(ArcWarcFilenameFilter.WARC_SUFFIX)
			|| name.endsWith(ArcWarcFilenameFilter.WARC_GZ_SUFFIX));	
	}
	
	public static Resource ARCArchiveRecordToResource(ArchiveRecord rec,
			ArchiveReader reader) throws ResourceNotAvailableException, IOException {

		if (!(rec instanceof ARCRecord)) {
			throw new ResourceNotAvailableException("Bad ARCRecord format");
		}
		ArcResource ar = new ArcResource((ARCRecord) rec, reader);
		ar.parseHeaders();
		return ar;
	}

	public static Resource WARCArchiveRecordToResource(ArchiveRecord rec,
			ArchiveReader reader) throws ResourceNotAvailableException, IOException {

		if (!(rec instanceof WARCRecord)) {
			throw new ResourceNotAvailableException("Bad WARCRecord format");
		}
		WarcResource wr = new WarcResource((WARCRecord) rec, reader);
		wr.parseHeaders();
		return wr;
	}
}
