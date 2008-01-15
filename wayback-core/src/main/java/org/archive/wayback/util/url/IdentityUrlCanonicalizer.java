package org.archive.wayback.util.url;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;

/**
 * Identity UrlCanonicalizer implementation, passing through urls as-is.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class IdentityUrlCanonicalizer implements UrlCanonicalizer {

	/* (non-Javadoc)
	 * @see org.archive.wayback.UrlCanonicalizer#urlStringToKey(java.lang.String)
	 */
	public String urlStringToKey(String url) throws URIException {
		return url;
	}
}
