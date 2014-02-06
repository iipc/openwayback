package org.archive.wayback.instantliveweb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.StatusLine;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.HttpHeaderOperation;
import org.jwat.common.RandomAccessFileInputStream;

public class InstaWarcResource extends Resource {
	
	public final static int TEMP_BUFF_SIZE = 8192;
	public final static String WARC_TMP = ".warc.tmp";
	
	protected HashMap<String, String> headers = new HashMap<String, String>();
	
	protected int maxInMemoryBuffer;
	protected long payloadRead = 0;
	protected long recordLength = 0;
	
	protected String tmpPrefix;
	protected RandomAccessFile tmpfile_raf = null;
	protected File tmpfile = null;
	
	protected ByteArrayOutputStream outBuff = null;
	
	protected int statusCode = 0;
	
	protected CaptureSearchResult captureResult = null;
	protected Header[] httpHeaders = null;
		
	public InstaWarcResource(String tmpPrefix, 
							int maxInMemoryBuffer,
							StatusLine statusLine,
							Header[] httpHeaders,
							InputStream data, long size, 
							MessageDigest payloadDigestObj) throws IOException
	{
		this.statusCode = statusLine.getStatusCode();
		this.tmpPrefix = tmpPrefix;
		this.maxInMemoryBuffer = maxInMemoryBuffer;
		this.httpHeaders = httpHeaders;
		
		this.load(httpHeaders, data, size, payloadDigestObj);
	}
	
	public InputStream createInputStream() throws IOException
	{
		if (tmpfile == null) {
			return new ByteArrayInputStream(outBuff.toByteArray());
		} else {
			tmpfile_raf.seek(0L);
			return new RandomAccessFileInputStream(tmpfile_raf);
		}
	}
	
	
	protected void load(Header[] httpHeaders, InputStream data, long size, MessageDigest payloadDigestObj) throws IOException
	{	
		int read = 0;
		
		if (data == null || size == 0) {
			outBuff = new ByteArrayOutputStream(0);
			return;
		}

		byte[] tmpBuf = new byte[TEMP_BUFF_SIZE];

		if ((size > 0) && (size <= this.maxInMemoryBuffer)) {
			outBuff = new ByteArrayOutputStream(this.maxInMemoryBuffer);
			
			while ((read = data.read(tmpBuf)) > 0) {
				payloadDigestObj.update(tmpBuf, 0, read);
				outBuff.write(tmpBuf, 0, read);
				payloadRead += read;
			}
			outBuff.close();
		} else {
			tmpfile = File.createTempFile(tmpPrefix, WARC_TMP);
			tmpfile_raf = new RandomAccessFile(tmpfile, "rw");
			tmpfile_raf.seek(0L);
			tmpfile_raf.setLength(0L);
			
			while ((read = data.read(tmpBuf)) > 0) {
				payloadDigestObj.update(tmpBuf, 0, read);
				tmpfile_raf.write(tmpBuf, 0, read);
				payloadRead += read;
			}
		}

		data.close();
	}
	
	protected void initHeaders(Header[] httpHeaders) throws IOException
	{
	    headers = new HashMap<String,String>();
	    
	    for (Header header: httpHeaders) {
	        headers.put(header.getName(), header.getValue());
	        if (header.getName().toUpperCase().contains(
	                HttpHeaderOperation.HTTP_TRANSFER_ENC_HEADER)) {
	            if (header.getValue().toUpperCase().contains(
	                    HttpHeaderOperation.HTTP_CHUNKED_ENCODING_HEADER)) {
	                setChunkedEncoding();
	            }
	        }
	    }
	}

	@Override
    public void close() throws IOException {
		if (tmpfile_raf != null) {
			tmpfile_raf.close();
		}

		if (tmpfile != null) {
			tmpfile.delete();
		}
    }

	@Override
    public int getStatusCode() {
		return statusCode;
    }
	
    public long getPayloadLength() {
		return payloadRead;
    }

	@Override
    public long getRecordLength() {
		return recordLength;
    }
	
	public void setRecordLength(long recordLength)
	{
		this.recordLength = recordLength;
	}

	@Override
    public Map<String, String> getHttpHeaders() {
	    // TODO Auto-generated method stub
	    return headers;
    }

	public CaptureSearchResult getCaptureResult() {
		return captureResult;
	}

	public void setCaptureResult(CaptureSearchResult captureResult) {
		this.captureResult = captureResult;
	}

	@Override
    public void parseHeaders() throws IOException {
	    super.setInputStream(this.createInputStream());
		this.initHeaders(httpHeaders);
    }
}
