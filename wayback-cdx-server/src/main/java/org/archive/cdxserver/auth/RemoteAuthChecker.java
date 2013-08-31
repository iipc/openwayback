package org.archive.cdxserver.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * An AuthChecker which determines if a url is allowed/restricted by checking a remote wayback /check-access interface
 * 
 * @author ilya
 *
 */
public class RemoteAuthChecker extends PrivTokenAuthChecker {

	protected String accessCheckUrl;
	
	@Override
    public boolean checkUrlAccess(String url)
    {
        if (accessCheckUrl == null) {
            return true;
        }
        
        InputStream is = null;
        
        try {
            is = new URL(accessCheckUrl + url).openStream();
            String result = IOUtils.toString(is);
            
            if (result.contains("allow")) {
                return true;
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
        
        return false;
    }
	
	public String getAccessCheckUrl() {
		return accessCheckUrl;
	}

	public void setAccessCheckUrl(String accessCheckUrl) {
		this.accessCheckUrl = accessCheckUrl;
	}
}
