package org.archive.wayback.instantliveweb;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.util.url.UrlOperations;

public class InstaArchiveUriConverter implements ResultURIConverter
{
	protected String recordPrefix = null;
	
	public InstaArchiveUriConverter()
	{
		
	}
	
	public void setRecordPrefix(String recordPrefix)
	{
		this.recordPrefix = recordPrefix;
	}
	
	public String getRecordPrefix()
	{
		return recordPrefix;
	}
	
	@Override
	public String makeReplayURI(String datespec, String url) {
		//Ignore the datespec
		
		StringBuilder sb = null;

		if (recordPrefix == null) {
			sb = new StringBuilder("/");
			sb.append(UrlOperations.stripDefaultPortFromUrl(url));
			return sb.toString();
		}
		
		if (url.startsWith(this.recordPrefix)) {
			return url;
		}
		
		sb = new StringBuilder(url.length());
		sb.append(this.recordPrefix);
		sb.append(UrlOperations.stripDefaultPortFromUrl(url));
		return sb.toString();
	}
}