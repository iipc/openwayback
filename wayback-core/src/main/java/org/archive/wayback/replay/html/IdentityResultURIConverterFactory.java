package org.archive.wayback.replay.html;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.proxy.ProxyHttpsResultURIConverter;

/**
 * {@link ContextResultURIConverterFactory} that simply returns given
 * {@link ResultURIConverter}, regardless of usage context.
 * <p>
 * Some {@link ReplayRenderer} wraps {@link ResultURIConverter} given with this
 * class to let {@link ReplayParseContext} use the same instance for all
 * context. Consider having {@code ResultURIConverter} implement
 * {@code ContextResultURIConverterFactory} (and return itself from
 * {@code getContextConverter(String)} method) to avoid this wrapping.
 * </p>
 * <p>
 * This class is better called <em>Context Independent</em> rather than
 * <em>Identity</em>. Perhaps this class may be dropped in the near future.
 * </p>
 * @deprecated 2015-02-10 no replacement.
 */
public class IdentityResultURIConverterFactory implements
		ContextResultURIConverterFactory {

	protected ResultURIConverter converter;

	/**
	 * constructor for returning {@link ProxyHttpsResultURIConverter}.
	 */
	public IdentityResultURIConverterFactory() {
		this.converter = new ProxyHttpsResultURIConverter();
	}

	/**
	 * constructor for returning <code>converter</code>.
	 * @param converter pre-built ResultURIConverter to be returned.
	 */
	public IdentityResultURIConverterFactory(ResultURIConverter converter) {
		this.converter = converter;
	}

	@Override
	public ResultURIConverter getContextConverter(String flags) {
		return converter;
	}
}
