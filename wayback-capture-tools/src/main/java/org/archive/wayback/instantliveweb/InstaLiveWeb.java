package org.archive.wayback.instantliveweb;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.IOUtils;
import org.archive.util.ArchiveUtils;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.accesscontrol.robotstxt.redis.RobotsTxtResource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.liveweb.NoRetryHandler;
import org.archive.wayback.util.IPRange;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.util.webapp.AbstractRequestHandler;
import org.archive.wayback.webapp.AccessPoint;
import org.archive.wayback.webapp.LiveWebRedirector;

import com.google.common.io.ByteStreams;

public class InstaLiveWeb extends AbstractRequestHandler implements LiveWebCache, LiveWebRedirector {

	private static final Logger LOGGER = Logger.getLogger(
			InstaLiveWeb.class.getName());
	
	final static String ORIG_MIME_TYPE = "origmimetype";
	
	final static String LIVEWEB_RUNTIME_ERROR_HEADER = "X-Archive-Wayback-Liveweb-Error";

	private static final String SPLIT_CONTENT_TYPE = "[; ]+";

	public static String EMBED_PREFIX = "_embed/";
	
	protected AccessPoint inner;
	
	protected String fullPathPrefix;
	protected long servePort = 6009;
	protected String localHostStr;
	
	protected InstaPersistCache persistCache;
	protected InstaLiveWebWarcWriter warcWriter;
	
	protected int numTries = 0;
	protected int retrySleep = 500;
	
	protected ResultURIConverter uriConverter;
	
    protected HttpConnectionManager connectionManager = null;
    protected HostConfiguration hostConfiguration = null;
    protected HttpClient http = null;

    protected String recordUserAgent = null;
    protected int timeoutMillis = 5000;
    protected int maxConnections = 500;
    
    protected Pattern refererRegex;
    
    protected List<IPRange> allowIPs;
    protected List<IPRange> blockedIPs;
	
	public void init()
	{
		try {
	        localHostStr = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        	localHostStr = "localhost";
        }
		
		fullPathPrefix = "http://" + localHostStr + ":" + servePort;
		
		StringBuilder sb = new StringBuilder("^.*(");
		sb.append(this.getAccessPointPath());
		sb.append("|");
		sb.append(this.getInner().getAccessPointPath());
		sb.append(")(");
		sb.append(EMBED_PREFIX);
		sb.append("|\\d{1,14}([^_]{0,2}_)?/)?(.*)$");
		
		
		refererRegex = Pattern.compile(sb.toString());
		
		initHttpLib();
		
		//File targetDir = new File(warcOutDir);
		// warcWriter = new InstaLiveWebWarcWriter(nameVersion, localHostStr, targetDir, warcPrefix, maxFileSize, maxResponseSize, cdxServer, "liveweb", null, null, null);
		
		if (warcWriter != null) {
			warcWriter.start(localHostStr);
		}
	}	
	
	@Override
    public boolean handleRequest(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws ServletException,
            IOException {
		
		String url = translateRequestPathQuery(httpRequest);
		
		boolean isEmbed = false;
		String referrer = httpRequest.getHeader("Referer");
				
		if (url.startsWith(EMBED_PREFIX)) {
			isEmbed = true;
			url = url.substring(EMBED_PREFIX.length());
						
			if (!isLiveWebReferrer(referrer)) {
				String redir = this.getAccessPointPath() + url;
				httpResponse.sendRedirect(redir);
				return true;
			}
		}
		
		boolean skipDedup = false;
		
		if ((referrer != null) && referrer.contains(getInner().getAccessPointPath())) {
			skipDedup = true;
		}
		
		if (!url.startsWith(UrlOperations.HTTP_SCHEME) &&
			!url.startsWith(UrlOperations.HTTPS_SCHEME)) {
			
			// Remove accidental calendar page requests
			if (url.startsWith("*/")) {
				url = url.substring(2);
			}
			
			// Assume http
			url = UrlOperations.HTTP_SCHEME + url;
		}
				
		InstaWarcResource warcResource = null;
		
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setAccessPoint(this.getInner());
		wbRequest.setRequestUrl(url);
		wbRequest.setReplayRequest();
		
		if (isEmbed) {
			wbRequest.setLiveWebEmbedRequest(true);
		} else {
			wbRequest.setLiveWebRequest(true);
		}
		
		try {
			URL theUrl = validateUrl(url);
			
			boolean ignoreRobots = isEmbed;
			
			warcResource = loadAndRecord(theUrl, skipDedup, ignoreRobots, httpRequest);
			
			if (warcResource == null) {
				//httpResponse.sendError(502, "Live Web Capture Failed");	
				//return true;
				throw new LiveDocumentNotAvailableException(url);
			}
			
			CaptureSearchResult result = warcResource.getCaptureResult();			
			wbRequest.setReplayTimestamp(result.getCaptureTimestamp());
			
			if (persistCache != null) {
				// If failed to persist, clear the result, its a failure
				if (!persistCache.saveResult(result)) {
					//httpResponse.sendError(502, "Live Web Persist Failed");
					//return true;
					throw new LiveWebCacheUnavailableException("Live Web Archiving has encountered an error");
				}
			}
			
			// Set Content-Location to the archival url of this recording
			String redirect = getInner().getUriConverter().makeReplayURI(result.getCaptureTimestamp(), result.getOriginalUrl());
			httpResponse.setHeader("Content-Location", redirect);

			if (!isEmbed && !getContentType(result).equals("text/html") && !result.getHttpCode().startsWith("3")) {
				handleRedirect(httpRequest, httpResponse, result, redirect);
			} else {
				handleServe(httpRequest, httpResponse, isEmbed, warcResource, result, url, wbRequest);
			}
			
        } catch (WaybackException e) {
			getInner().logError(httpResponse, LIVEWEB_RUNTIME_ERROR_HEADER, e, wbRequest);
			getInner().getException().renderException(httpRequest, httpResponse, wbRequest, e, getUriConverter());
		} catch(Exception e) {
			getInner().logError(httpResponse, LIVEWEB_RUNTIME_ERROR_HEADER, e, wbRequest);
		} finally {
			if (warcResource != null) {
				warcResource.close();
			}
		}
		
		return true;
	}
	
	
	protected boolean isLiveWebReferrer(String referrer) {
		if (referrer == null) {
			return false;
		}
		
		if (referrer.contains(this.getAccessPointPath())) {
			return true;
		}
		
		return false;
    }

	protected URL validateUrl(String url) throws BadQueryException, LiveDocumentNotAvailableException {
		URL validUrl = null;
		
		try {
			validUrl = new URL(url);
			
			if (blockedIPs == null) {
				return validUrl;
			}
			
			String host = validUrl.getHost();
			
			InetAddress address = InetAddress.getByName(host);
			
			String ip = address.getHostAddress();
			
			boolean blocked = false;
			
			for (IPRange range : blockedIPs) {
				if (range.contains(ip)) {
					blocked = true;
					break;
				}
			}
			
			if (!blocked || allowIPs == null) {
				return validUrl;
			}
			
			for (IPRange range : allowIPs) {
				if (range.contains(ip)) {
					blocked = false;
					break;
				}
			}
			
			if (blocked) {
				throw new LiveDocumentNotAvailableException(url);
			}
			
		} catch (MalformedURLException mal) {
			throw new BadQueryException(mal.toString());
		} catch (UnknownHostException e) {
			throw new LiveDocumentNotAvailableException(e.toString());
		}
		
		return validUrl;
    }

	protected void handleServe(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, boolean isEmbed,
            InstaWarcResource warcResource, 
            CaptureSearchResult captureResult,
            String url, 
            WaybackRequest wbRequest) throws ServletException, IOException, WaybackException {
				
		CaptureSearchResults results = new CaptureSearchResults();
		results.addSearchResult(captureResult);
		
		warcResource.parseHeaders();
				
		ReplayRenderer renderer = getInner().getReplay().getRenderer(wbRequest, captureResult, warcResource);
		
        renderer.renderResource(httpRequest, httpResponse, wbRequest, captureResult, warcResource, getUriConverter(), results);
    }

	protected void handleRedirect(HttpServletRequest httpRequest, 
								  HttpServletResponse httpResponse, 
								  CaptureSearchResult result,
								  String redirect) throws IOException, LiveWebTimeoutException
	{		
		if ((numTries > 0) && !verifyCached(result, numTries, retrySleep)) {
			//httpResponse.sendError(503, "Live Web Persist Taking Too Long");
			//return;
			throw new LiveWebTimeoutException("Live Web Persist Taking Too Long");
		}

		httpResponse.sendRedirect(redirect);	
	}
	
	protected boolean verifyCached(CaptureSearchResult result, int numTries, int retrySleep)
	{
		HttpClient client = http;
		
		HeadMethod method = new HeadMethod(result.getFile());
		
		String rangeHeader = HTTPSeekableLineReader.makeRangeHeader(result.getOffset(), (int)result.getCompressedLength());
		method.setRequestHeader("Range", rangeHeader);
		
		boolean success = false;
		
		for (int i = 0; i < numTries; i++) {
			try {
				int responseStatus = client.executeMethod(method);
				
				if ((responseStatus == 206) || (responseStatus == 200)) {
					success = true;
					break;
				}
				
				LOGGER.severe("*** Failed with " + responseStatus + " on " + (i + 1) + " try of "+ rangeHeader + " " + result.getFile() + ", sleeping for " + retrySleep);

				Thread.sleep(retrySleep);
				
			} catch (IOException io) {
				method.abort();
			} catch (InterruptedException e) {

            }
		}
		
		return success;
	}
	
	protected void initHttpLib()
	{
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
		HttpClientParams params = new HttpClientParams();
        params.setParameter(HttpClientParams.RETRY_HANDLER, new NoRetryHandler());
        if (recordUserAgent != null) {
        	params.setParameter(HttpClientParams.USER_AGENT, recordUserAgent);
        }
    	http = new HttpClient(params, connectionManager);
    	http.setHostConfiguration(hostConfiguration);
    	
    	// ConnectionManager Params
    	HttpConnectionManagerParams connMgrParams = connectionManager.getParams();
  
    	connMgrParams.setMaxTotalConnections(this.maxConnections);
    	connMgrParams.setMaxConnectionsPerHost(hostConfiguration, this.maxConnections);   
  
    	connMgrParams.setConnectionTimeout(this.timeoutMillis);
    	connMgrParams.setSoTimeout(this.timeoutMillis);
   	}
	
	public static class LaxGetMethod extends GetMethod {
		
		URI theURI;
		String requestLine;
		
		public LaxGetMethod(String uri) throws URIException {
			super();
			theURI = new org.archive.url.LaxURI(uri, true);	
			setURI(theURI);
		}
		
		@Override
	    protected void writeRequestLine(HttpState state, HttpConnection conn) throws IOException, HttpException
	    {
	    	String queryString = getQueryString();
	    	
	    	// HttpMethodBase.generateRequestLine() does not allow queries that start with '?'.
	    	// If '?' is first char of the query string, it'll assume that its an implict '?' and remove it.
	    	// Here, if we get a '?', it should be part of the query string, that is, the query should start with '??'
	    	// so we add an extra '?' here since it won't be added later.
	    	if (queryString != null && queryString.startsWith("?")) {
	    		queryString = "?" + queryString;
	    	}
	    	
	        requestLine = HttpMethodBase.generateRequestLine(conn, getName(),
	                getPath(), queryString, this.effectiveVersion.toString());
	        
	        conn.print(requestLine, getParams().getHttpElementCharset());
	    }

	    public URI getURI() throws URIException {
	    	return theURI;
	    }
	    
	    public String getRequestLine()
	    {
	    	return requestLine;
	    }
	}
	
	protected InstaWarcResource loadAndRecord(URL theURL, boolean skipDedup, boolean ignoreRobots, HttpServletRequest httpRequest) throws AccessControlException, BadQueryException
	{	
		
		String timestamp = null;
		Date date = new Date();
		
		HttpClient client = http;
		LaxGetMethod method;
		int responseStatus = 0;
		
		String url = theURL.toString();
		
		try {
			method = createGetMethod(url);
			method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			addRequestHeaders(httpRequest, method);

		} catch (URIException e) {
			throw new BadQueryException(e.toString());
        } catch (IllegalArgumentException ill) {
        	throw new BadQueryException(ill.toString());
        }
		
		CaptureSearchResult capture = new FastCaptureSearchResult();
		
		if (ignoreRobots) {
			capture.setRobotIgnore();
		}
		
		InstaWarcResource warcResource = null;
		//boolean success = false;
		
		try {
			responseStatus = client.executeMethod(method);
			
			if (responseStatus >= 500 || responseStatus < 200) {
				method.abort();
				return null;
			}
				
			// RequestHeaders			
			StringBuilder requestHeaders = new StringBuilder();
			
			requestHeaders.append(method.getRequestLine());
			
			for (Header header : method.getRequestHeaders()) {
				requestHeaders.append(header.toString());
			}
			
			setContentType(capture, method.getResponseHeader("Content-Type"));

			
			if (getInner().getSelfRedirectCanonicalizer() != null) {
				capture.setUrlKey(getInner().getSelfRedirectCanonicalizer().urlStringToKey(url));
			}
			
			timestamp = ArchiveUtils.get14DigitDate(date);
			
			capture.setOriginalUrl(url);
			capture.setCaptureTimestamp(timestamp);
			capture.setHttpCode(String.valueOf(responseStatus));
			
			warcResource = warcWriter.record(url, date.getTime(), 
					requestHeaders.toString(), 
					method.getStatusLine(),
					method.getResponseHeaders(),
					method.getResponseBodyAsStream(),
					method.getResponseContentLength(), 
					capture,
					skipDedup);
			
			if (fullPathPrefix != null) {
				String fullPath = capture.getCustom(InstaLiveWebWarcWriter.FULL_PATH);
				if (fullPath != null) {
					capture.putCustom(InstaLiveWebWarcWriter.FULL_PATH, fullPathPrefix + fullPath);
				} else {
					capture.setFile(fullPathPrefix + capture.getFile());
				}
			}
			
			//success = true;
			
		} catch (Exception e) {
			if (method != null) {
				method.abort();
			}
			
			if (warcResource != null) {
				try {
	                warcResource.close();
                } catch (IOException io) {

                }
                warcResource = null;
			}
			
			if (e instanceof AccessControlException) {
				throw (AccessControlException)e;
			}
			LOGGER.severe("Error Recording " + url);
			e.printStackTrace();
			
			return null;
		} finally {
			
			if (method != null) {
				method.releaseConnection();
			}
		}
		
		return warcResource;
    }
	
	protected boolean passHeader(String name, GetMethod method, HttpServletRequest request)
	{
		String value = request.getHeader(name);
		if (value == null) {
			return false;
		}
		method.setRequestHeader(name, value);
		return true;
	}
	
	protected void addRequestHeaders(
			HttpServletRequest httpRequest,
            GetMethod method) {
		
		passHeader("Accept", method, httpRequest);
		passHeader("Accept-Charset", method, httpRequest);
		passHeader("Accept-Language", method, httpRequest);
		
		//UA
		String ua = httpRequest.getHeader("User-Agent");
		if (ua != null) {
			ua = ua + " (via Wayback Save Page)";
		} else {
			ua = recordUserAgent;
		}
		
		method.setRequestHeader("User-Agent", ua);
		
		// Referer
		String referer = httpRequest.getHeader("Referer");
		
		if (referer != null) {
			Matcher refMatcher = refererRegex.matcher(referer);
			if (refMatcher.matches() && refMatcher.groupCount() == 4) {
				referer = refMatcher.group(4);
			}
			method.setRequestHeader("Referer", referer);
		}
	}

	public static String parseContentType(String contentType)
	{
		String[] firstVal = contentType.split(SPLIT_CONTENT_TYPE);	
		return firstVal[0].toLowerCase();
	}

	public static void setContentType(CaptureSearchResult capture,
            Header contentType) {
		
		if (contentType != null) {
			capture.setMimeType(parseContentType(contentType.getValue()));
		} else {
			capture.setMimeType(FastCaptureSearchResult.EMPTY_VALUE);
		}
		capture.putCustom(ORIG_MIME_TYPE, capture.getMimeType());	    
    }
	
	public static String getContentType(CaptureSearchResult capture)
	{
		return capture.getCustom(ORIG_MIME_TYPE);
	}
	
    public LiveWebState handleRedirect(WaybackException we,
            WaybackRequest wbRequest, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {

		if ((wbRequest == null) || wbRequest.isIdentityContext()) {
			return LiveWebState.NOT_FOUND;
		}
		
		if (we.getStatus() != 404) {
			return LiveWebState.NOT_FOUND;
		}
		
		GetMethod method = null;
		boolean success = false;
		String contentType = null;
		
		try {
			String url = wbRequest.getRequestUrl();
			
			method = createGetMethod(url);
			
			int responseStatus = http.executeMethod(method);
			
			Header contentTypeHeader = method.getResponseHeader("Content-Type");
			
			if (contentTypeHeader != null) {
				contentType = parseContentType(contentTypeHeader.getValue());
			}
			
			if (responseStatus >= 400 || responseStatus < 200) {
				success = false;
			} else {
				success = true;
			}

		} catch (Exception e) {
			success = false;
		} finally {
			if (method != null) {
				method.abort();
				method.releaseConnection();
			}
		}
		
		if (!success) {
			return LiveWebState.NOT_FOUND;
		}
		
		String redirUrl = null;
		
		// First, check for embed replay context
		boolean doRedirectToLiveWeb = wbRequest.isAnyEmbeddedContext() || wbRequest.isAjaxRequest();
		
		if (!doRedirectToLiveWeb) {
			// Check for content type, if not html, then do redirect to save
			if ((contentType != null) && !contentType.equals("text/html")) {
				doRedirectToLiveWeb = true;
			}
		}
		
		if (doRedirectToLiveWeb) {
			redirUrl = this.getAccessPointPath() + EMBED_PREFIX + wbRequest.getRequestUrl();
			httpResponse.sendRedirect(redirUrl);
			return LiveWebState.REDIRECTED;
		}
		
		return LiveWebState.FOUND;
    }
	
	protected LaxGetMethod createGetMethod(String url) throws URIException {
		LaxGetMethod method = new LaxGetMethod(url);
		
		method.setFollowRedirects(false);
		method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		method.setRequestHeader("Connection", "close");
		return method;
    }

	public InstaPersistCache getPersistCache() {
		return persistCache;
	}

	public void setPersistCache(InstaPersistCache persistCache) {
		this.persistCache = persistCache;
	}

	public InstaLiveWebWarcWriter getWarcWriter() {
		return warcWriter;
	}

	public void setWarcWriter(InstaLiveWebWarcWriter warcWriter) {
		this.warcWriter = warcWriter;
	}

	public int getNumTries() {
		return numTries;
	}

	public void setNumTries(int numTries) {
		this.numTries = numTries;
	}

	public int getRetrySleep() {
		return retrySleep;
	}

	public void setRetrySleep(int retrySleep) {
		this.retrySleep = retrySleep;
	}

	public ResultURIConverter getUriConverter() {
		return uriConverter;
	}

	public void setUriConverter(ResultURIConverter uriConverter) {
		this.uriConverter = uriConverter;
	}

	public AccessPoint getInner() {
		return inner;
	}

	public void setInner(AccessPoint inner) {
		this.inner = inner;
	}

	public String getRecordUserAgent() {
		return recordUserAgent;
	}

	public void setRecordUserAgent(String recordUserAgent) {
		this.recordUserAgent = recordUserAgent;
	}
	
	public List<IPRange> getBlockedIPs() {
		return blockedIPs;
	}

	public void setBlockedIPs(List<IPRange> blockedIPs) {
		this.blockedIPs = blockedIPs;
	}
	
	public List<IPRange> getAllowIPs() {
		return allowIPs;
	}

	public void setAllowIPs(List<IPRange> allowIPs) {
		this.allowIPs = allowIPs;
	}
	
	
	// This is used only for live robots.txt streaming -- maybe separate to its own class

	protected int maxLiveWebCacheResource = 500000;

	@Override
    public Resource getCachedResource(URL url, long maxCacheMS,
            boolean record) throws LiveDocumentNotAvailableException,
            LiveWebCacheUnavailableException, LiveWebTimeoutException,
            IOException {
		
		GetMethod method = createGetMethod(url.toString());
		method.setFollowRedirects(true);

		try {
			int responseStatus = http.executeMethod(method);
			
			if (responseStatus >= 300 || responseStatus < 200) {
				throw new LiveDocumentNotAvailableException("Invalid Status: " + responseStatus);
			}		
			
			InputStream in = ByteStreams.limit(method.getResponseBodyAsStream(), maxLiveWebCacheResource);
			return new RobotsTxtResource(IOUtils.toString(in));
			
		} catch (IOException io) {
			throw new LiveDocumentNotAvailableException(io.toString());
		} finally {
			method.abort();
			method.releaseConnection();
		}
    }

	@Override
    public void shutdown() {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public String getLiveWebPrefix() {
		return this.getAccessPointPath();
    }
}
