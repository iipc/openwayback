package org.archive.cdxserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.cdxserver.auth.AuthChecker;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.url.UrlSurtRangeComputer;
import org.archive.url.WaybackURLKeyMaker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;

@Controller
public class BaseCDXServer implements InitializingBean {
	
	protected UrlSurtRangeComputer urlSurtRangeComputer;
	protected WaybackURLKeyMaker canonicalizer = null;
	protected AuthChecker authChecker;
	
	protected boolean surtMode = false;

	public boolean isSurtMode() {
		return surtMode;
	}

	public void setSurtMode(boolean surtMode) {
		this.surtMode = surtMode;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		canonicalizer = new WaybackURLKeyMaker(surtMode);
		urlSurtRangeComputer = new UrlSurtRangeComputer(surtMode);
	}
	
	public String canonicalize(String url, boolean surt) throws UnsupportedEncodingException, URISyntaxException
	{
		if ((canonicalizer == null) || (url == null) || url.isEmpty()) {
			return url;
		}
		
		url = java.net.URLDecoder.decode(url, "UTF-8");
		
		if (surt) {
			return url;
		}
		
		int slashIndex = url.indexOf('/');
		// If true, assume this is already a SURT and skip
		if ((slashIndex > 0) && url.charAt(slashIndex - 1) == ')') {
			return url;
		}
						
		return canonicalizer.makeKey(url);
	}
	
    protected PrintWriter getGzipWriter(HttpServletResponse response) throws IOException
    {
		response.setHeader("Content-Encoding", "gzip");
		PrintWriter writer = new PrintWriter(new GZIPOutputStream(response.getOutputStream())
		{
//			{
//			    def.setLevel(Deflater.BEST_COMPRESSION);
//			}
		});
		
		return writer;
    }
	
	protected void prepareResponse(HttpServletResponse response)
	{
		response.setContentType("text/plain; charset=\"UTF-8\"");		
	}
	
	protected AuthToken initAuthToken(HttpServletRequest request)
	{
	    if (authChecker == null) {
	        //TODO: Think more about the security implications
	        return AuthToken.createAllAccessToken();
	    }
	    
	    return authChecker.createAuthToken(request);
	}
		
	public AuthChecker getAuthChecker() {
		return authChecker;
	}

	public void setAuthChecker(AuthChecker authChecker) {
		this.authChecker = authChecker;
	}
}