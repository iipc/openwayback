package org.archive.wayback.accesscontrol.robotstxt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.archive.wayback.webapp.PerformanceLogger;

class RobotsContext implements BaseHttpConnMan.ConnectionCallback
{
	private final static Logger LOGGER = Logger
	.getLogger(RobotsContext.class.getName());
	
	final static int LIVE_OK = 200;
	final static int LIVE_TIMEOUT_ERROR = 900;
	final static int LIVE_HOST_ERROR = 910;
	final static int LIVE_INVALID_TYPE_ERROR = 920;
	
	final static int MAX_ROBOTS_SIZE = 500000;
	
	final CountDownLatch latch;
	final String url;
	final String current;
	final boolean cacheFails;
	final long created;
	
	private int status;
	private String newRobots;
	
	long startTime;

	RobotsContext(String url, String current, boolean cacheFails, boolean singleWait)
	{
		this.latch = (!singleWait ? new CountDownLatch(1) : null);
		this.url = url;
		this.current = current;
		this.created = System.currentTimeMillis();
		this.cacheFails = cacheFails;
	}
	
	boolean isValid()
	{
		return (newRobots != null) && (status == LIVE_OK);
	}
	
	boolean isSingleWait()
	{
		return (this.latch == null);
	}
	
	static boolean isErrExpiry(int status)
	{
		return (status == 0) || ((status >= 500) && (status != LIVE_HOST_ERROR));
	}
	
	boolean isErrExpiry()
	{
		return isErrExpiry(status);
	}
	
	static boolean isErrExpiry(String code)
	{
		try {
			int status = Integer.parseInt(code);
			return isErrExpiry(status);
		} catch (NumberFormatException n) {
			return true;
		}
	}
		
	String getNewRobots()
	{
		return newRobots;
	}
	
	void setNewRobots(String newRobots)
	{
		this.newRobots = newRobots;
	}
	
	int getStatus()
	{
		return status;
	}
	
	void setStatus(int status)
	{
		this.status = status;
	}
	
	long getCreated()
	{
		return created;
	}
	
	@Override
	public boolean supportStatus(int status) {
		setStatus(status);
		if (status != LIVE_OK) {
			PerformanceLogger.noteElapsed("HttpLoadSuccess", System.currentTimeMillis() - startTime, url + " " + getStatus());
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void doRead(int numToRead, String contentType, InputStream input, String charset) throws IOException, InterruptedException {
		if ((contentType == null) || (contentType.indexOf("text/plain") < 0)) {
			LOGGER.info("Questionable Content-Type: " + contentType + " for: " + url);
		}
				
		if ((numToRead <= 0) || (numToRead > MAX_ROBOTS_SIZE)) {
			numToRead = MAX_ROBOTS_SIZE;
		}

		ByteArrayOutputStream baos = readMaxBytes(input, numToRead);
									
		if (charset == null) {
			charset = "utf-8";
		}
		
		baos.flush();
		String contents = null;
		
		try {
			contents = baos.toString(charset);
		} catch (UnsupportedEncodingException uex) {
			contents = baos.toString();
		}
		
		baos.close();
		
		setNewRobots(contents);
		PerformanceLogger.noteElapsed("HttpLoadSuccess", System.currentTimeMillis() - startTime, url + " " + getStatus() + ((contents != null) ? " Size: " + contents.length() : " NULL"));
	}

	@Override
	public void handleException(Exception exc) {
		int status = 0;
		
		if (exc instanceof InterruptedIOException) {
			status = LIVE_TIMEOUT_ERROR; //Timeout (gateway timeout)
		} else if (exc instanceof InterruptedException) {
			status = LIVE_TIMEOUT_ERROR;
		} else if (exc instanceof UnknownHostException) {
			status = LIVE_HOST_ERROR;
		}
		
		setStatus(status);
		
		PerformanceLogger.noteElapsed("HttpLoadFail", System.currentTimeMillis() - startTime, 
				"Exception: " + exc + " url: " + url + " status " + status);				
	}
	
	private ByteArrayOutputStream readMaxBytes(InputStream input, int max) throws IOException, InterruptedException
	{
		byte[] byteBuff = new byte[8192];
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(max);
		
		int totalRead = 0;
			
		while (true) {
			Thread.sleep(0);
			
			int toRead = Math.min(byteBuff.length, max - totalRead);
			
			if (toRead <= 0) {
				break;
			}
			
			int numRead = input.read(byteBuff, 0, toRead);
			
			if (numRead < 0) {
				break;
			}
			
			totalRead += numRead;
			
			baos.write(byteBuff, 0, numRead);
		}
		
		return baos;
	}
}