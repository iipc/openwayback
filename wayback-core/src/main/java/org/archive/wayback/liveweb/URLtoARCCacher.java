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
package org.archive.wayback.liveweb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.archive.httpclient.HttpRecorderGetMethod;
import org.archive.io.RecordingInputStream;
import org.archive.io.ReplayInputStream;
import org.archive.io.arc.ARCWriter;
import org.archive.net.LaxURI;
import org.archive.util.Recorder;
import org.archive.wayback.util.ByteOp;

/**
 * 
 * Takes an input URL String argument, downloads, stores in an ARCWriter,
 * and returns a FileRegion consisting of the compressed ARCRecord containing
 * the response, or a forged, "fake error response" ARCRecord which can be
 * used to send the content to an OutputStream. 
 * 
 * @author brad
 *
 */
public class URLtoARCCacher {
	private static final Logger LOGGER = Logger.getLogger(
			URLtoARCCacher.class.getName());

	private static String CONTENT_TYPE_HEADER = "Content-Type".toLowerCase();
	private static String GET_METHOD_NAME = "GET";
	
	private static String DEFAULT_RECORDER_DIR = "/var/tmp/brad/recorder"; 
	private File recorderCacheDir = new File(DEFAULT_RECORDER_DIR);
	
	private static String DEFAULT_BACKING_FILE_BASE = "recorder-tmp"; 
	private String backingFileBase = DEFAULT_BACKING_FILE_BASE;
    private String userAgent = "genericUserAgent";
    private int connectionTimeoutMS = 10000;
    private int socketTimeoutMS = 10000;
	private int outBufferSize = 1024 * 100;
	private int inBufferSize = 1024 * 100;
//	private int outBufferSize = 10;
//	private int inBufferSize = 100;
	private final static HttpMethodRetryHandler noRetryHandler = 
		new NoRetryHandler();
	
	private final ThreadLocal<HttpClient> tl = new ThreadLocal<HttpClient>() {

		protected synchronized HttpClient initialValue() {
			HttpClientParams params = new HttpClientParams();
            params.setParameter(HttpClientParams.RETRY_HANDLER, noRetryHandler);
    		IPHttpConnectionManager manager = new IPHttpConnectionManager();
    		Protocol dnsTimedProtocol = new Protocol("http",
    				new DNSTimingProtocolSocketFactory(), 80);
    		Protocol.registerProtocol("http", dnsTimedProtocol);
    		manager.getParams().setConnectionTimeout(connectionTimeoutMS);
    		manager.getParams().setSoTimeout(socketTimeoutMS);
    		return new HttpClient(params, manager);
        }
    };

    private HttpClient getHttpClient() {
        return tl.get();
    }
	
	
	private static byte[] ERROR_BYTES = "HTTP 502 Bad Gateway\n\n".getBytes();
	private static String ERROR_MIME  = "unk";	
	private static String ERROR_IP    = "0.0.0.0";

	private static byte[] TIMEOUT_BYTES = "HTTP 504 Gateway Timeout\n\n".getBytes();
	private static String TIMEOUT_MIME  = "unk";
	private static String TIMEOUT_IP    = "0.0.0.0";

	/**
	 * @param url to cache
	 * @param cache ARCCacheDirectory for storing result or faked result
	 * @return FileRegion of compressed byte range for ARCRecord.
	 * @throws IOException for the usual reasons
	 * @throws URIException if url argument isn't really an URL..
	 */
	public FileRegion cacheURL(String url, ARCCacheDirectory cache)
		throws IOException, URIException {

		FileRegion region = null;

		// to track if we got a response (any response) or an exception.
		boolean gotUrl = false;
		boolean isTimeout = false;
		String fName = backingFileBase + "-" + Thread.currentThread().getId();
		Recorder recorder = new Recorder(recorderCacheDir,fName,
				outBufferSize, inBufferSize);
		
		ExtendedGetMethod getMethod = null;

		// TWO STEPS:
		// first do the GET, using a Recorder to get the response.
		// then, if that worked, save the recorded value into an ARC 
        // 		and return it's region
		// if we didn't get a response, forge a fake record and return that.
		try {
			Recorder.setHttpRecorder(recorder);
			LaxURI lURI = new LaxURI(url,true);
			getMethod = new ExtendedGetMethod(url,recorder);
			getMethod.setURI(lURI);
			HttpClient client = getHttpClient();
			getMethod.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
			getMethod.setFollowRedirects(false);
			getMethod.setRequestHeader("User-Agent", userAgent);
			int code = client.executeMethod(getMethod);
			LOGGER.info("URL(" + url + ") HTTP:" + code);
			InputStream responseIS = getMethod.getResponseBodyAsStream();
			if(responseIS != null) {
				ByteOp.discardStream(responseIS);
				responseIS.close();
			}
			gotUrl = true;

		} catch (URIException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			LOGGER.warning("Unknown host for " + url);

		} catch (ConnectTimeoutException e) {
			// TODO: should we act like it's a full block?
			LOGGER.warning("Timeout out connecting to " + url);
			isTimeout = true;
		} catch(SocketTimeoutException e) {
			LOGGER.warning("Timeout out socket for " + url);
			isTimeout = true;
		} catch (ConnectException e) {
			LOGGER.warning("ConnectionRefused to " + url);
		} catch (NoRouteToHostException e) {
			LOGGER.warning("NoRouteToHost for " + url);
		} catch (SocketException e) {
			// should only be things like "Connection Reset", etc..
			LOGGER.warning("SocketException for " + url);		
		} catch (HttpException e) {
			e.printStackTrace();
			// we have to let IOExceptions out, problems caused by local disk
			// NEED to return errors, indicating that there is not an 
			// authoritative answer, and thus... NOTHING can be shown.
//		} catch (IOException e) {
//			e.printStackTrace();
		} finally {
			recorder.closeRecorders();
			Recorder.setHttpRecorder(null);
			if(getMethod != null) {
				getMethod.releaseConnection();
			}
		}

		// now write the content, or a fake record:
		ARCWriter writer = null;
		ReplayInputStream replayIS = null;
		try {
			writer = cache.getWriter();
			if(gotUrl) {

				RecordingInputStream ris = recorder.getRecordedInput();
				replayIS = ris.getReplayInputStream();
				region = storeInputStreamARCRecord(writer, url, 
						getMethod.getMime(), getMethod.getRemoteIP(),
						getMethod.getCaptureDate(), 
						replayIS, (int) ris.getSize());
			} else if(isTimeout) {
				region = storeTimeout(writer,url);
			} else {
				region = storeNotAvailable(writer, url);
			}

		} finally {
			IOUtils.closeQuietly(replayIS);
			if(writer != null) {
				cache.returnWriter(writer);
			}
		}
		recorder.close();

		return region;
	}

	private FileRegion storeInputStreamARCRecord(ARCWriter writer,
			String url, String mime, String ip,	Date captureDate, 
			InputStream is, int length) throws IOException {

		writer.checkSize();
		final long arcOffset = writer.getPosition();
		final String arcPath = writer.getFile().getAbsolutePath();

		writer.write(url,mime,ip,captureDate.getTime(),length,is);
		writer.checkSize();
//		long newSize = writer.getPosition();
		long oSize = writer.getFile().length();
//		final long arcEndOffset = oSize;
		LOGGER.info("Wrote " + url + ": " + arcPath + "(" + arcOffset
				+ "-" + oSize + ")");

		FileRegion fr = new FileRegion();
		fr.file = writer.getFile();
		fr.start = arcOffset;
		fr.end = oSize;
		fr.isFake = false;
		return fr;
	}

	private FileRegion storeNotAvailable(ARCWriter writer, String url) 
		throws IOException {
		
		ByteArrayInputStream bais = new ByteArrayInputStream(ERROR_BYTES);
		FileRegion fr = storeInputStreamARCRecord(writer, url,
				ERROR_MIME, ERROR_IP, new Date(), bais, ERROR_BYTES.length);
		fr.isFake = true;
		return fr;
	}
	
	private FileRegion storeTimeout(ARCWriter writer, String url) 
		throws IOException {
	
		ByteArrayInputStream bais = new ByteArrayInputStream(TIMEOUT_BYTES);
		FileRegion fr = storeInputStreamARCRecord(writer, url,
				TIMEOUT_MIME, TIMEOUT_IP, new Date(), bais, TIMEOUT_BYTES.length);
		fr.isFake = true;
		return fr;
	}

	/*
	 * Get method which ferrets away the Content-Type header, the remote IP
	 * and remembers when the HTTP Message header was received.
	 */
	private class ExtendedGetMethod extends HttpRecorderGetMethod {
		
		/**
		 * @param uri to be fetched
		 * @param recorder which is not currently used by base class, but 
		 * we're going to require and send it on anyways.
		 */
		public ExtendedGetMethod(String uri, Recorder recorder) {
			super(uri, recorder);
		}

		private String remoteIP = "";
		private Date captureDate = null;
		private String mime = "unk";

		public String getName() {
			return GET_METHOD_NAME;
		}

		protected void processStatusLine(HttpState state, HttpConnection conn) {
			// grab the remote IP, and record when we started getting bytes..
			// Sam thinks we should somehow record how fast we got it back..
			// and then replay it at the same rate we received it.
	
			captureDate = new Date();
			IPStoringHttpConnection bhc = (IPStoringHttpConnection) conn;
			remoteIP = bhc.getRemoteIP();
		}
		protected void processResponseBody(HttpState state, HttpConnection conn) {
			// grab the mime..
			Header headers[] = this.getResponseHeaders();
			for (int i = 0; i < headers.length; i++) {
				String lcHeader = headers[i].getName().toLowerCase();
				if(lcHeader.compareTo(CONTENT_TYPE_HEADER) == 0) {
					mime = headers[i].getValue();
				}
			}
		}

		/**
		 * @return Returns the captureDate.
		 */
		public Date getCaptureDate() {
			return captureDate;
		}

		/**
		 * @return Returns the mime.
		 */
		public String getMime() {
			return mime;
		}
		
		/**
		 * @return Returns the remoteIP.
		 */
		public String getRemoteIP() {
			return remoteIP;
		}
	}

	/**
	 * HttpConnectionManager that returns IPHttpConnection objects, for
	 * accessing the IP address
	 */
	private class IPHttpConnectionManager extends SimpleHttpConnectionManager {
		public HttpConnection getConnection(HostConfiguration hostConfiguration) {
			IPStoringHttpConnection conn = new IPStoringHttpConnection(hostConfiguration);
	        conn.setHttpConnectionManager(this);
	        conn.getParams().setDefaults(this.getParams());
	        return conn;
		}

		public HttpConnection getConnectionWithTimeout(
				HostConfiguration hostConfiguration, long timeout) {
			// TODO: is this  lying? have we really set the time out?
			IPStoringHttpConnection conn =
				new IPStoringHttpConnection(hostConfiguration);
	        conn.setHttpConnectionManager(this);
	        conn.getParams().setDefaults(this.getParams());
	        return conn;
		}

		public HttpConnection getConnection(
				HostConfiguration hostConfiguration, long timeout) {

			return new IPStoringHttpConnection(hostConfiguration);
		}
	    public void releaseConnection(HttpConnection conn) {
	        // ensure connection is closed
	        conn.close();
	        InputStream lastResponse = conn.getLastResponseInputStream();
	        if (lastResponse != null) {
	            conn.setLastResponseInputStream(null);
	            try {
	                lastResponse.close();
	            } catch (IOException ioe) {
	                //FIX ME: badness - close to force reconnect.
	                conn.close();
	            }
	        }
	    }
	}

	/**
	 * HttpConnection that allows access to the IP address which was
	 * used for the connection.
	 */
	private class IPStoringHttpConnection extends HttpConnection {

		/**
		 * @param hc HostConfiguration
		 */
		public IPStoringHttpConnection(HostConfiguration hc) {
			super(hc);
		}
		/**
		 * @return the remote IP address that was connected to, as a String  
		 */
		public String getRemoteIP() {
			return getSocket().getInetAddress().getHostAddress();
		}
	}

	/**
	 * @return the recorderCacheDir
	 */
	public String getRecorderCacheDir() {
		return recorderCacheDir.getAbsolutePath();
	}

	/**
	 * @param recorderCacheDirPath the recorderCacheDir to set
	 */
	public void setRecorderCacheDir(String recorderCacheDirPath) {
		this.recorderCacheDir = new File(recorderCacheDirPath);
	}

	/**
	 * @return the backingFileBase
	 */
	public String getBackingFileBase() {
		return backingFileBase;
	}

	/**
	 * @param backingFileBase the backingFileBase to set
	 */
	public void setBackingFileBase(String backingFileBase) {
		this.backingFileBase = backingFileBase;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the connectionTimeoutMS
	 */
	public int getConnectionTimeoutMS() {
		return connectionTimeoutMS;
	}

	/**
	 * @param connectionTimeoutMS the connectionTimeoutMS to set
	 */
	public void setConnectionTimeoutMS(int connectionTimeoutMS) {
		this.connectionTimeoutMS = connectionTimeoutMS;
	}

	/**
	 * @return the socketTimeoutMS
	 */
	public int getSocketTimeoutMS() {
		return socketTimeoutMS;
	}

	/**
	 * @param socketTimeoutMS the socketTimeoutMS to set
	 */
	public void setSocketTimeoutMS(int socketTimeoutMS) {
		this.socketTimeoutMS = socketTimeoutMS;
	}
		
}
