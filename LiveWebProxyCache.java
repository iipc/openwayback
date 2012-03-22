package org.archive.wayback.accesscontrol.robotstxt;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;
import org.archive.wayback.webapp.PerformanceLogger;

public class LiveWebProxyCache implements LiveWebCache {
	private final static Logger LOGGER = Logger
			.getLogger(LiveWebProxyCache.class.getName());
	
	/* SOCKET SETTINGS / PARAMS */
	protected BaseHttpConnMan connMan = null;

	protected int responseTimeoutMS = 10000;
	
	protected String userAgent;
	
	/* THREAD WORKER SETTINGS */
	
	protected int maxNumUpdateThreads = 1000;
	protected int maxCoreUpdateThreads = 75;
	
	protected int threadKeepAliveTime = 5000;
	
	protected ThreadPoolExecutor refreshService;
	
	/* CLEANUP */
	private IdleCleanerThread idleCleaner;
	private int idleCleanupTimeoutMS = 300000;
	
	public BaseHttpConnMan getConnMan() {
		return connMan;
	}

	public void setHttpConnMan(BaseHttpConnMan connMan) {
		this.connMan = connMan;
	}

	public int getMaxNumUpdateThreads() {
		return maxNumUpdateThreads;
	}

	public void setMaxNumUpdateThreads(int maxNumUpdateThreads) {
		this.maxNumUpdateThreads = maxNumUpdateThreads;
	}

	public int getMaxCoreUpdateThreads() {
		return maxCoreUpdateThreads;
	}

	public void setMaxCoreUpdateThreads(int maxCoreUpdateThreads) {
		this.maxCoreUpdateThreads = maxCoreUpdateThreads;
	}
	
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void init() {
		LOGGER.setLevel(Level.FINER);
		
		refreshService = new ThreadPoolExecutor(maxCoreUpdateThreads, maxNumUpdateThreads, threadKeepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		
		idleCleaner = new IdleCleanerThread(idleCleanupTimeoutMS);
		idleCleaner.setDaemon(true);
		idleCleaner.start();
	}
	
	protected void appendLogInfo(PrintWriter info)
	{
        info.println("=== Idle Cleanup Stats ===");
        info.println("  Active: " + refreshService.getActiveCount());
        info.println("  Pool: " + refreshService.getPoolSize());
        info.println("  Largest: " + refreshService.getLargestPoolSize());
        connMan.appendLogInfo(info);
	}
	
	private class IdleCleanerThread extends Thread {
	    
	    private int timeout;
	    
	    public IdleCleanerThread(int timeout) {
	        this.timeout = timeout;
	    }

	    @Override
	    public void run() {
	        try {
                while (true) {
                    connMan.idleCleanup();
                    StringWriter buff = new StringWriter();
                    PrintWriter info = new PrintWriter(buff);
                    appendLogInfo(info);
                    LOGGER.info(buff.getBuffer().toString());
                    sleep(timeout);
                 }
	        } catch (InterruptedException ex) {
	            // terminate
	        }
	    }
	    
	    public void shutdown() {
	    	this.interrupt();
	    }   
	}
	
	protected class LoadArcCallback implements BaseHttpConnMan.ConnectionCallback, Runnable
	{
		long startTime;
		int status = 0;
		String url;
		ArcResource resource;
		
		LoadArcCallback(String url)
		{
			this.url = url;
			startTime = System.currentTimeMillis();
		}
		
		@Override
		public boolean supportStatus(int status) {
			this.status = status;
			if (status != 200) {
				PerformanceLogger.noteElapsed("LiveProxySuccess", System.currentTimeMillis() - startTime, url + " " + status);
				return false;
			} else {
				return true;
			}
		}

		@Override
		public void doRead(int length, String contentType, InputStream input,
				String charset) throws IOException {
			
    		ARCRecord r = new ARCRecord(
    				new GZIPInputStream(input),
    				"id",0L,false,false,true);
    		try {
				this.resource = (ArcResource) 
					ResourceFactory.ARCArchiveRecordToResource(r, null);
				PerformanceLogger.noteElapsed("LiveProxySuccess", System.currentTimeMillis() - startTime, url);
			} catch (ResourceNotAvailableException e) {
				this.resource = null;
				PerformanceLogger.noteElapsed("LiveProxyFail", System.currentTimeMillis() - startTime, url + " " + e);	
			}
		}

		@Override
		public void handleException(Exception exc) {
			this.resource = null;
			PerformanceLogger.noteElapsed("LiveProxyFail", System.currentTimeMillis() - startTime, url + " " + exc);	
		}

		@Override
		public void run() {
			connMan.loadProxyLive(this, this.url, userAgent);
		}
	}	

	@Override
	public Resource getCachedResource(URL urlURL, long maxCacheMS,
			boolean bUseOlder) throws LiveDocumentNotAvailableException,
			LiveWebCacheUnavailableException, LiveWebTimeoutException,
			IOException {
		
		String url = urlURL.toExternalForm();
		
		LoadArcCallback callback = new LoadArcCallback(url);
		Future<?> future = refreshService.submit(callback);
		
		try {
			future.get(responseTimeoutMS, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			LOGGER.info("LIVE PROXY ERROR: " + e);
		}

		if (callback.resource == null) {
    		throw new LiveDocumentNotAvailableException(url);
		}
		
		if (callback.resource.getStatusCode() == 502) {
			throw new LiveDocumentNotAvailableException(url);
		} else if(callback.resource.getStatusCode() == 504) {
			throw new LiveWebTimeoutException("Timeout:" + url);
		}
		
		return callback.resource;
	}
	
	

	@Override
	public void shutdown() {
		if (refreshService != null) {
			refreshService.shutdown();
		}
		
		if (idleCleaner != null) {
			idleCleaner.shutdown();
		}
	}

}
