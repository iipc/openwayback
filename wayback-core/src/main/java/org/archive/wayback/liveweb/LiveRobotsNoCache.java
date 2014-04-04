package org.archive.wayback.liveweb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.archive.wayback.accesscontrol.robotstxt.redis.RobotsTxtResource;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;

import com.google.common.io.ByteStreams;

public class LiveRobotsNoCache extends RemoteLiveWebCache {

	protected int maxRobotsSize = 512000;

	public int getMaxRobotsSize() {
		return maxRobotsSize;
	}

	public void setMaxRobotsSize(int maxRobotsSize) {
		this.maxRobotsSize = maxRobotsSize;
	}

	@Override
    public Resource getCachedResource(URL url, long maxCacheMS,
            boolean record) throws LiveDocumentNotAvailableException,
            LiveWebCacheUnavailableException, LiveWebTimeoutException,
            IOException {
		
		HttpClient http = super.getHttpClient();
		
		GetMethod method = new GetMethod(url.toString());
		method.setFollowRedirects(true);
		method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		
		InputStream in = null;

		try {
			int responseStatus = http.executeMethod(method);
			
			if (responseStatus >= 400 || responseStatus < 200) {
				throw new LiveDocumentNotAvailableException("Invalid Status: " + responseStatus);
			}		
			
			in = ByteStreams.limit(method.getResponseBodyAsStream(), maxRobotsSize);
			return new RobotsTxtResource(IOUtils.toString(in));
			
		} catch (IOException io) {
			throw new LiveDocumentNotAvailableException(io.toString());
		} finally {
			if (in != null) {
				in.close();
			}
			
			method.abort();
			method.releaseConnection();
		}
    }

	@Override
    public void shutdown() {
	    // TODO Auto-generated method stub
    }
}
