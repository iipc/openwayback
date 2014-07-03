package org.archive.wayback.replay;

import java.util.Map;

/**
 * base class for {@link HttpHeaderProcessor} that preserves original headers
 * by prepending header name with given prefix.
 * 
 * <p>use {@link #preserve(Map, String, String)} for headers that should not be
 * preserved if {@code prefix} is empty. use {@link #preserveAlways(Map, String, String)}
 * for headers that need to be preserved regardless of {@code prefix}.</p>
 * 
 * @author Kenji Nagahashi
 *
 */
public abstract class PreservingHttpHeaderProcessor implements HttpHeaderProcessor {

	protected String prefix = null;

	public String getPrefix() {
		return prefix;
	}

	/**
	 * prefix prepended to the name of headers preserved.
	 * 
	 * <p>Example: "{@code X-Archive-Orig-}". 
	 * Empty String is translated to {@code null}. 
	 * Default value is {@code null}.</p>
	 * @param prefix header name prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		if (this.prefix.isEmpty())
			this.prefix = null;
	}

	/**
	 * add a header {@code prefix + name} with value {@code value} to {@code output}.
	 * if {@code prefix} is either null or empty, this method is no-op.
	 * @param output headers Map
	 * @param name header name
	 * @param value header value
	 */
	protected void preserve(Map<String, String> output, String name, String value) {
		if (prefix != null) {
			output.put(prefix + name, value);
		}
	}

	/**
	 * add a header {@code prefix + name} with value {@code value} to {@code output}.
	 * if {@code prefix} is either null or empty, header is added with original name.
	 * @param output headers Map
	 * @param name header name
	 * @param value header value
	 */
	protected void preserveAlways(Map<String, String> output, String name, String value) {
		if (prefix == null) {
			output.put(name, value);
		} else {
			output.put(prefix + name, value);
		}
	}
}