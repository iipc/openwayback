package org.archive.wayback.webapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.resourcestore.locationdb.FlatFileResourceFileLocationDB;

public class CustomUserResourceIndex {
	
	protected UrlCanonicalizer canonicalizer;
	
	protected FlatFileResourceFileLocationDB indexFile = new FlatFileResourceFileLocationDB();
	protected String filename;
	
	public void setIndexFile(String file) throws IOException
	{
		filename = file;
		indexFile.setPath(file);
	}
	
	public String getIndexFile()
	{
		return filename;
	}
	
	public String[] getCustomResources(WaybackRequest request)
	{
		String canonKey = null;
		try {
			canonKey = canonicalizer.urlStringToKey(request.getRequestUrl());
			return indexFile.nameToUrls(canonKey);

		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}
	
	// Extracts all fields fieldNum from matching lines and returns as a json array
	public String getCustomResourcesPathsAsJSON(WaybackRequest request, String replayPrefix, int fieldNum)
	{
		String[] matchingLines = getCustomResources(request);
		
		if (matchingLines == null || matchingLines.length == 0) {
			return "";
		}
		
		StringBuilder builder = new StringBuilder("[");
		
		for (int i = 0; i < matchingLines.length; i++) {
			if (i > 0) {
				builder.append(",\n");
			}
			
			String[] fields = matchingLines[i].split(indexFile.getDelimiter());
			String url = replayPrefix + fields[fieldNum];
			builder.append("\"");
			try {
				builder.append(URLEncoder.encode(url, "UTF-8"));
			} catch (UnsupportedEncodingException e) {

			}
			builder.append("\"");
		}
		
		builder.append("]");
		
		return builder.toString();
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}
}
