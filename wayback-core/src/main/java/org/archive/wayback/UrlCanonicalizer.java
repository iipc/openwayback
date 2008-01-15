package org.archive.wayback;

import org.apache.commons.httpclient.URIException;

public interface UrlCanonicalizer {
	public String urlStringToKey(String url) throws URIException;
}
