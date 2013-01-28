package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

class TimedDNSLookup
{
	private final static Logger LOGGER = 
		Logger.getLogger(TimedDNSLookup.class.getName());
	
	private ExecutorService executor;
	private int dnsTimeoutMS;
	
	public ExecutorService getExecutor() {
		return executor;
	}

	public int getDnsTimeoutMS() {
		return dnsTimeoutMS;
	}

	public TimedDNSLookup(int poolSize, int dnsTimeoutMS) {		
		executor = Executors.newFixedThreadPool(poolSize);
		this.dnsTimeoutMS = dnsTimeoutMS;
	}

	public InetAddress[] resolveHostname(String host)
			throws UnknownHostException {
		
		Future<InetAddress[]> future = executor.submit(new InetLookup(host));
		
		try {
			return future.get(dnsTimeoutMS, TimeUnit.MILLISECONDS);
			
		} catch (ExecutionException e) {
			throw (UnknownHostException)e.getCause();
		} catch (InterruptedException e) {
			future.cancel(true);
			LOGGER.warning("DNS INTERRUPTED: " + host);
			throw new UnknownHostException(host);
		} catch (TimeoutException e) {
			future.cancel(true);
			LOGGER.warning("DNS TIMEOUT: " + host);
			throw new UnknownHostException(host);
		}
	}
	
	private class InetLookup implements Callable<InetAddress[]>
	{
		String host;
		
		private InetLookup(String host)
		{
			this.host = host;
		}
		
		@Override
		public InetAddress[] call() throws Exception {
			return InetAddress.getAllByName(host);
		}
	}
}