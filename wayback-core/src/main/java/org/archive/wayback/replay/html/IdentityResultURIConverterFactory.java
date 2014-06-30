package org.archive.wayback.replay.html;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.proxy.ProxyHttpsResultURIConverter;

/**
 * {@link ContextResultURIConverterFactory} that simply returns given {@link ResultURIConverter},
 * regardless of usage context.
 * <p>This class is better called <em>Context Independent</em> rather than <em>Identity</em>.</p>
 *
 */
public class IdentityResultURIConverterFactory implements
		ContextResultURIConverterFactory {
	
	protected ResultURIConverter converter;
	
	/**
	 * constructor for returning {@link ProxyHttpsResultURIConverter}.
	 */
	public IdentityResultURIConverterFactory()
	{
		this.converter = new ProxyHttpsResultURIConverter();
	}
	/**
	 * constructor for returning <code>converter</code>.
	 * @param converter pre-built ResultURIConverter to be returned.
	 */
	public IdentityResultURIConverterFactory(ResultURIConverter converter)
	{
		this.converter = converter;
	}

	@Override
	public ResultURIConverter getContextConverter(String flags) {
		return converter;
	}
}
