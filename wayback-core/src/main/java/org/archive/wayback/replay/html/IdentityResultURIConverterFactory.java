package org.archive.wayback.replay.html;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.proxy.ProxyHttpsResultURIConverter;

public class IdentityResultURIConverterFactory implements
		ContextResultURIConverterFactory {
	
	protected ResultURIConverter converter;
	
	public IdentityResultURIConverterFactory()
	{
		this.converter = new ProxyHttpsResultURIConverter();
	}
	
	public IdentityResultURIConverterFactory(ResultURIConverter converter)
	{
		this.converter = converter;
	}

	@Override
	public ResultURIConverter getContextConverter(String flags) {
		return converter;
	}
}
