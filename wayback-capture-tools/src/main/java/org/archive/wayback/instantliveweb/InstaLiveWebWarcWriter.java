package org.archive.wayback.instantliveweb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.StatusLine;
import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthChecker;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.filter.CDXAccessFilter;
import org.archive.cdxserver.writer.CDXListWriter;
import org.archive.format.cdx.CDXLine;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.resourceindex.cdxserver.AccessCheckFilter;
import org.jwat.common.Base32;
import org.jwat.common.Uri;
import org.jwat.warc.WarcConstants;
import org.jwat.warc.WarcDigest;
import org.jwat.warc.WarcHeader;
import org.jwat.warc.WarcRecord;
import org.jwat.warc.WarcWriter;
import org.jwat.warc.WarcWriterFactory;

import com.google.common.io.ByteStreams;

/**
 * A WARC writer for instant live web (based on DK LapWarcWriter)
 * 
 * @author ilya
 */
public class InstaLiveWebWarcWriter {

	private static final Logger LOGGER = Logger
	        .getLogger(InstaLiveWebWarcWriter.class.getName());

	public final static String WARC_GZ = ".warc.gz";
	public final static String WARC_TMP = ".warc.tmp";

	public final static String WARC_REVISIT = "warc/revisit";
	public final static String REVISIT_FILTER = "!mimetype:" + WARC_REVISIT;
	
	public final static String WARC_PATH_FIELD = "warcPath";

	public final static int TEMP_BUFF_SIZE = 8192;
	
	public final static String ENCODING = "ISO-8859-1";

	static final String FULL_PATH = "fullpath";

	protected String nameVersion = "";

	protected DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

	protected File warcOutDir;

	protected String warcPrefix;

	protected long maxFileSize = 100000000L;
	protected long maxResponseSize = maxFileSize;
	protected int maxInMemoryBuffer = (1024 * 512);

	protected String warcStartDate;

	protected int warcCount;

	protected String hostname;
	protected String tmpPrefix;

	protected String extension;
	
	protected String isPartOf;

	protected String warcFields;

	protected File file;
	protected FileOutputStream fileout;
	protected FileLock lock;

	protected WarcWriter writer;
	protected Uri warcinfoRecordId = null;

	protected CDXServer cdxDedupServer;

	protected boolean started = false;
	
	public InstaLiveWebWarcWriter()
	{
		
	}

	public InstaLiveWebWarcWriter(
			String nameVersion,
			String hostname,
	        File warcOutDir,
	        String warcPrefix,
	        long maxFileSize,
	        long maxResponseSize,
	        CDXServer cdxDedupServer,

	        String isPartOf) {

		this.nameVersion = nameVersion;
		this.hostname = hostname;
		this.warcOutDir = warcOutDir;
		this.warcPrefix = warcPrefix;
		this.maxFileSize = maxFileSize;
		this.maxResponseSize = maxResponseSize;

		this.isPartOf = isPartOf;

		this.cdxDedupServer = cdxDedupServer;
	}

	public static String createWarcHeaderFields(String version,
	        String hostname, String isPartOf, String description,
	        String operator, String httpheader) {
		StringBuilder sb = new StringBuilder();
		sb.append("software");
		sb.append(": ");
		sb.append(version);
		sb.append("\r\n");
		sb.append("host");
		sb.append(": ");
		sb.append(hostname);
		sb.append("\r\n");
		if (isPartOf != null && isPartOf.length() > 0) {
			sb.append("isPartOf");
			sb.append(": ");
			sb.append(isPartOf);
			sb.append("\r\n");
		}
		if (description != null && description.length() > 0) {
			sb.append("description");
			sb.append(": ");
			sb.append(description);
			sb.append("\r\n");
		}
		if (operator != null && operator.length() > 0) {
			sb.append("operator");
			sb.append(": ");
			sb.append(operator);
			sb.append("\r\n");
		}
		if (httpheader != null && httpheader.length() > 0) {
			sb.append("httpheader");
			sb.append(": ");
			sb.append(httpheader);
			sb.append("\r\n");
		}
		sb.append("format");
		sb.append(": ");
		sb.append("WARC file version 1.0");
		sb.append("\r\n");
		sb.append("conformsTo");
		sb.append(": ");
		sb.append("http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf");
		sb.append("\r\n");
		return sb.toString();
	}

	protected synchronized void closeWriter() throws IOException {
		if (writer != null) {
			writer.close();
			writer = null;
		}
		
		if (lock != null) {
			lock.release();
			lock = null;
		}
		
		if (fileout != null) {
			fileout.close();
			fileout = null;
		}

		warcinfoRecordId = null;
	}

	protected synchronized void ensureWriter() throws Exception {

		if ((file != null) && (file.length() <= maxFileSize)) {
			return;
		}

		closeWriter();
		
		warcStartDate = dateFormat.format(new Date());
		
		StringBuffer sb = new StringBuffer(warcPrefix);
		sb.append("-");
		sb.append(this.warcStartDate);
		sb.append("-");
		sb.append(hostname);
		sb.append(extension);
		
		String filename = sb.toString();

		file = new File(warcOutDir, filename);

		fileout = new FileOutputStream(file, true);
		lock = fileout.getChannel().lock(); 
			
		writer = WarcWriterFactory.getWriter(fileout, TEMP_BUFF_SIZE, true);

		warcinfoRecordId = new Uri("urn:uuid:" + UUID.randomUUID());

		byte[] warcFieldBytes = warcFields.getBytes(ENCODING);
		
		WarcRecord record;
		WarcHeader header;
		
		try {
			record = WarcRecord.createRecord(writer);
			header = record.header;
			header.warcTypeIdx = WarcConstants.RT_IDX_WARCINFO;
			header.warcDate = new Date();
			header.warcFilename = filename;
			header.warcRecordIdUri = warcinfoRecordId;
			header.contentTypeStr = WarcConstants.CT_APP_WARC_FIELDS;
			header.contentLength = new Long(warcFieldBytes.length);
			writer.writeHeader(record);
			writer.writePayload(warcFieldBytes);
		} finally {
			writer.closeRecord();
			fileout.flush();
		}
	}

	protected void checkWritableDirs(File dir) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Target is not a directory: '"
			        + dir);
		} else if (!dir.canWrite()) {
			throw new IllegalArgumentException(
			        "Target directory is not writable: '" + dir);
		}
	}

	protected synchronized void start(String hostname) {
		if (started) {
			return;
		}
		
		this.hostname = hostname;
		
		checkWritableDirs(warcOutDir);
		
		this.tmpPrefix = hostname + "-" + isPartOf;

		warcCount = 0;

		extension = WARC_GZ;

		warcFields = createWarcHeaderFields(nameVersion, hostname, isPartOf, null, null, null);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					stopWriter(false);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		Thread.currentThread().setUncaughtExceptionHandler(
		        new Thread.UncaughtExceptionHandler() {
			        @Override
			        public void uncaughtException(Thread t, Throwable e) {
				        System.err.println("Uncaught exception in thread '" + t
				                + "'");
				        e.printStackTrace();
				        try {
					        stopWriter(true);
				        } catch (IOException e1) {
					        throw new RuntimeException(e);
				        }
			        }
		        });

		started  = true;
		LOGGER.info("started !");
	}

	protected synchronized void stop(boolean error) {
		try {
			stopWriter(error);
			started = false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void stopWriter(boolean error) throws IOException {
		if (writer != null) {
			LOGGER.info("closed writer");
			closeWriter();
			writer = null;
		}
	}

	protected CDXLine getDedupCDX(String url, String digest, boolean ignoreRobots)
	        throws AccessControlException {
		
		if (cdxDedupServer == null) {
			return null;
		}

		CDXListWriter listWriter = new CDXListWriter();

		CDXQuery query = new CDXQuery(url);
		query.setFilter(new String[] { CDXLine.digest + ":" + digest, REVISIT_FILTER });
		// query.setFilter(new String[] { REVISIT_FILTER });		
		// Read last
		query.setLimit(-1);
		
		AuthToken auth = new AuthToken();
		auth.setIgnoreRobots(ignoreRobots);

		try {
			cdxDedupServer.getCdx(query, auth, listWriter);
		} catch (IOException e) {
			// No dedup info
			return null;
		} catch (RuntimeException re) {
			Throwable cause = re.getCause();

			// Propagate AccessControlException
			if (cause instanceof AccessControlException) {
				throw (AccessControlException) cause;
			}

			return null;
		}

		if (!listWriter.getCDXLines().isEmpty()) {
			CDXLine line = listWriter.getCDXLines().get(0);
			// Just check the last line for the digest
			if (line.getDigest().equals(digest)) {
				return line;
			}
		}

		return null;
	}

	protected synchronized void writeRecords(String uri,
	        long requestTimestamp,

	        CaptureSearchResult fillResult,

	        byte[] requestHeaderBytes,
	        byte[] responseHeaderBytes,

	        InputStream payloadStream, 
	        long payloadSize,
	        
	        WarcDigest warcPayloadDigest,
	        CDXLine origLine

	) throws IOException, URISyntaxException, AccessControlException {
		Uri responseRecordID = new Uri("urn:uuid:" + UUID.randomUUID());

		WarcRecord record;
		WarcHeader header;

		boolean isRevisit = (origLine != null);
			
		// Response/Revisit Record
		fillResult.setOffset(file.length());

		try {
			record = WarcRecord.createRecord(writer);
			header = record.header;
			header.warcTypeIdx = (isRevisit ? WarcConstants.RT_IDX_REVISIT : WarcConstants.RT_IDX_RESPONSE);
			header.warcDate = new Date(requestTimestamp);

			header.warcRecordIdUri = responseRecordID;
			header.warcWarcinfoIdUri = warcinfoRecordId;
			header.warcTargetUriStr = uri;

			header.warcPayloadDigest = warcPayloadDigest;
			header.contentTypeStr = "application/http; msgtype=response";

			if (isRevisit) {
				header.warcProfileUri = new Uri("http://netpreserve.org/warc/0.18/revisit/identical-payload-digest");
				header.warcRefersToTargetUriStr = origLine.getFilename();
				header.warcRefersToDate = ArchiveUtils.getDate(
				        origLine.getTimestamp(), null);

				header.contentLength = (long) responseHeaderBytes.length;

				writer.writeHeader(record);
				writer.writePayload(responseHeaderBytes);

				fillResult.setMimeType(WARC_REVISIT);

			} else {
				header.contentLength = responseHeaderBytes.length + payloadSize;

				writer.writeHeader(record);
				writer.writePayload(responseHeaderBytes);
				writer.streamPayload(payloadStream);
			}
		} finally {
			writer.closeRecord();
		}

		// Fill CaptureSearchResult info for CDX
		fillResult.setCompressedLength(file.length() - fillResult.getOffset());
		fillResult.setDigest(warcPayloadDigest.digestString);
		fillResult.setFile(file.getName());
		//fillResult.putCustom(WARC_PATH_FIELD, file.getParent());
		fillResult.putCustom(FULL_PATH, file.getParent() + "/");
		
		try {
			// Request header
			record = WarcRecord.createRecord(writer);
			header = record.header;
			header.warcTypeIdx = WarcConstants.RT_IDX_REQUEST;
			header.warcDate = new Date(requestTimestamp);
			header.warcRecordIdUri = new Uri("urn:uuid:" + UUID.randomUUID());
			header.addHeader(WarcConstants.FN_WARC_CONCURRENT_TO, responseRecordID, null);
			header.warcWarcinfoIdUri = warcinfoRecordId;
			header.warcTargetUriStr = uri;
			header.contentTypeStr = "application/http; msgtype=request";
			header.contentLength = new Long(requestHeaderBytes.length);
			writer.writeHeader(record);
			writer.writePayload(requestHeaderBytes);
		} finally {
			writer.closeRecord();
		}
		
		// Force output to disk
		fileout.flush();
	}

	public InstaWarcResource record(String uri, long requestTimestamp,
	        String requestHeaders, StatusLine statusLine,
	        Header[] responseHeaders, InputStream data, long size,
	        CaptureSearchResult fillResult, boolean skipDedup) throws Exception {
		
		InstaWarcResource warcResource = null;
		
		try {
			
			if ((maxResponseSize > 0) && (size > maxResponseSize)) {
				data = ByteStreams.limit(data, maxResponseSize);
				size = maxResponseSize;
			}

			MessageDigest payloadDigestObj = MessageDigest.getInstance("SHA1");
			payloadDigestObj.reset();
			
			warcResource = new InstaWarcResource(tmpPrefix, this.maxInMemoryBuffer, statusLine, responseHeaders, data, size, payloadDigestObj);
	
	
			byte[] requestHeaderBytes = requestHeaders.getBytes(ENCODING);
			
			// Filter response headers
			byte[] responseHeaderBytes = filter(responseHeaders, statusLine, size);
			
			// Ensure writer exists
			ensureWriter();

			
			byte[] payloadDigestBytes = payloadDigestObj.digest();
	
			final WarcDigest warcPayloadDigest = WarcDigest.createWarcDigest(
			        "SHA1", payloadDigestBytes, "base32",
			        Base32.encodeArray(payloadDigestBytes));
			
			CDXLine origLine = null;
			AccessControlException saveAe = null;
			
			try {
				// No revisit checking if payload is 0, or if skipping dedup
				if (skipDedup || (warcResource.getPayloadLength() == 0)) {
					accessCheckCapture(fillResult, fillResult.isRobotIgnore());
				} else {
					// Still perform access check
					origLine = this.getDedupCDX(uri, warcPayloadDigest.digestString, fillResult.isRobotIgnore());
				}
			} catch (AccessControlException ae) {
				// Save AccessControlException so we can record the record anyway
				saveAe = ae;
			}
	
			// Write request + response/revisit records
			writeRecords(uri, requestTimestamp, fillResult,
			        requestHeaderBytes, responseHeaderBytes, warcResource.createInputStream(), warcResource.getPayloadLength(), warcPayloadDigest, origLine);
			
			warcResource.setRecordLength(fillResult.getCompressedLength());
			
			warcResource.setCaptureResult(fillResult);
	
			// If access control exception, throw it now		
			if (saveAe != null) {
				throw saveAe;
			}
			
		} catch (Exception e) {
			if (warcResource != null) {
				warcResource.close();
				warcResource = null;
			}
			throw e;
		}
		
		return warcResource;
	}

	protected boolean accessCheckCapture(CaptureSearchResult fillResult, boolean ignoreRobots) throws AccessControlException {
		if (this.cdxDedupServer == null) {
			return true;
		}
		
		AuthChecker checker = this.cdxDedupServer.getAuthChecker();
		
		if (checker == null) {
			return true;
		}
		
		AuthToken auth = new AuthToken();
		auth.setIgnoreRobots(ignoreRobots);
		
		CDXAccessFilter filter = checker.createAccessFilter(auth);
		
		if (filter == null) {
			return true;
		}
		
		try {
			// TODO: avoid cast? Slightly optimized version, allows for skipping robots
			if (filter instanceof AccessCheckFilter) {
				return ((AccessCheckFilter)filter).include(fillResult, true);
			}
			
			return filter.includeUrl(fillResult.getUrlKey(), fillResult.getOriginalUrl());
		} catch (RuntimeException re) {
			Throwable cause = re.getCause();

			// Propagate AccessControlException
			if (cause instanceof AccessControlException) {
				throw (AccessControlException) cause;
			}
			
			return false;
		}
    }

	public static byte[] filter(Header[] headers, StatusLine statusLine,
	        long contentLength) throws IOException {

		StringBuilder sb = new StringBuilder();

		sb.append(statusLine.toString());
		sb.append("\r\n");

		boolean bContentLengthPresent = false;

		for (Header header : headers) {
			String name = header.getName();

			if ("content-length".equalsIgnoreCase(name)) {
				sb.append(header.getName());
				sb.append(": ");
				sb.append(Long.toString(contentLength));
				sb.append("\r\n");

				bContentLengthPresent = true;
				// } else if ("content-encoding".equalsIgnoreCase(name)) {
				// //Skip?
				// } else if ("transfer-encoding".equalsIgnoreCase(name)) {
				// //Skip?
			} else if ("transfer-encoding".equalsIgnoreCase(name)) {
				// Skip transfer-encoding! the client de-chunks the response
			} else {
				sb.append(header.toString());
			}
		}

		if (!bContentLengthPresent) {
			sb.append("Content-Length: ");
			sb.append(Long.toString(contentLength));
			sb.append("\r\n");
		}

		sb.append("\r\n");

		byte[] responseHeaderBytes = sb.toString().getBytes(ENCODING);
		return responseHeaderBytes;
	}
	
	public String getNameVersion() {
		return nameVersion;
	}

	public void setNameVersion(String nameVersion) {
		this.nameVersion = nameVersion;
	}
	
	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public long getMaxResponseSize() {
		return maxResponseSize;
	}

	public void setMaxResponseSize(long maxResponseSize) {
		this.maxResponseSize = maxResponseSize;
	}
	
	public String getWarcOutDir() {
		return warcOutDir.getAbsolutePath();
	}

	public void setWarcOutDir(String warcOutDir) {
		this.warcOutDir = new File(warcOutDir);
	}

	public String getWarcPrefix() {
		return warcPrefix;
	}

	public void setWarcPrefix(String warcPrefix) {
		this.warcPrefix = warcPrefix;
	}

	public CDXServer getCdxDedupServer() {
		return cdxDedupServer;
	}

	public void setCdxDedupServer(CDXServer cdxDedupServer) {
		this.cdxDedupServer = cdxDedupServer;
	}

	public String getIsPartOf() {
		return isPartOf;
	}

	public void setIsPartOf(String isPartOf) {
		this.isPartOf = isPartOf;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}
