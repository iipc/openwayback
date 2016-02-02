package org.archive.wayback.replay;

import java.util.Map;
import java.util.Set;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.url.UrlOperations;

/**
 * base class for {@link HttpHeaderProcessor} that preserves original headers by
 * prepending header name with given prefix.
 * 
 * <p>
 * use {@link #preserve(Map, String, String)} for headers that should not be
 * preserved if {@code prefix} is empty. use
 * {@link #preserveAlways(Map, String, String)} for headers that need to be
 * preserved regardless of {@code prefix}.
 * </p>
 * <p>
 * This class now has base implementation of {@code filter} methods, which
 * covers three types of headers: <i>PassThrough</i>, <i>Rewrite</i> and
 * <i>Drop</i>.
 * <p>
 * @author Kenji Nagahashi
 *
 */
public abstract class PreservingHttpHeaderProcessor implements HttpHeaderProcessor {

	protected String prefix = null;

	protected Set<String> passThroughHeaders = null;
	protected Set<String> rewriteHeaders = null;
	protected Set<String> dropHeaders = null;

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
		if (this.prefix != null && this.prefix.isEmpty())
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

	@Override
	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, CaptureSearchResult result) {
		String ucKey = key.toUpperCase();
		if (dropHeaders != null && dropHeaders.contains(ucKey))
			preserve(output, key, value);
		else
			preserveAlways(output, key, value);
		// rewrite header fields with URL values
		if (rewriteHeaders != null && rewriteHeaders.contains(ucKey)) {
			String baseUrl = result.getOriginalUrl();
			String ts = result.getCaptureTimestamp();
			// by the spec, value should be absolute already, but may
			// not be in practice.
			String url = UrlOperations.resolveUrl(baseUrl, value);
			output.put(key,  uriConverter.makeReplayURI(ts, url));
		} else if (passThroughHeaders != null && passThroughHeaders.contains(ucKey)) {
			output.put(key, value);
		}
	}

	@Override
	public void filter(Map<String, String> output, String key, String value,
			ReplayRewriteContext context) {
		String ucKey = key.toUpperCase();
		if (dropHeaders != null && dropHeaders.contains(ucKey))
			preserve(output, key, value);
		else
			preserveAlways(output, key, value);
		// rewrite header fields with URL values
		if (rewriteHeaders != null && rewriteHeaders.contains(ucKey)) {
			String replayUrl = context.contextualizeUrl(value, "hd_");
			output.put(key,  replayUrl);
		} else if (passThroughHeaders != null && passThroughHeaders.contains(ucKey)) {
			output.put(key, value);
		}
	}

}