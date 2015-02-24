package org.archive.wayback.proxy;

import org.apache.commons.httpclient.URIException;
import org.archive.url.UsableURIFactory;
import org.archive.wayback.ReplayURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.replay.ReplayContext;
import org.archive.wayback.replay.ReplayURLTransformer;

/**
 * {@link ReplayURIConverter} for proxy mode, in which all URLs can be replayed
 * as they are.
 * <p>
 * This class also provides default {@link ReplayURLTransformer} implementation that rewrites
 * all {@code https://} links to {@code http://} (all non-full URLs are unaffected.)
 * This is meant for forcing all replay requests to go through HTTP proxy (Unfortunately
 * it won't always work with the latest browsers implementing HSTS).
 * To disable this feature, set {@code rewriteHttps} to {@code false}.
 * </p>
 */
public class ProxyHttpsReplayURIConverter implements ReplayURIConverter, ReplayURLTransformer {

	private boolean rewriteHttps = true;

	public boolean isRewriteHttps() {
		return rewriteHttps;
	}

	/**
	 * Weather to rewrite {@code https://} to {@code http://}.
	 * Default {@code true}.
	 * @param rewriteHttps {@code true} to rewrite
	 */
	public void setRewriteHttps(boolean rewriteHttps) {
		this.rewriteHttps = rewriteHttps;
	}

	@Override
	public String makeReplayURI(String datespec, String url) {
		return makeReplayURI(datespec, url, null, URLStyle.ABSOLUTE);
	}

	/**
	 * @return {@code url} as it is except for rewriting {@code https://} to {@code http://}
	 * if {@link #isRewriteHttps()}.
	 * (Caveat: does not recognize escaped URLs such as {@code https:\/\/...}.
	 */
	@Override
	public String makeReplayURI(String datespec, String url, String flags,
			URLStyle urlStyle) {
		if (!isRewriteHttps()) return url;
		if (url.startsWith(WaybackConstants.HTTPS_URL_PREFIX)) {
			url = WaybackConstants.HTTP_URL_PREFIX + url.substring(WaybackConstants.HTTPS_URL_PREFIX.length());
		}
		return url;
	}

	@Override
	public ReplayURLTransformer getURLTransformer() {
		return this;
	}

	/**
	 * Rewrites {@code https://} to {@code http://} if {@link #isRewriteHttps()}.
	 */
	@Override
	public String transform(ReplayContext replayContext, String url,
			String contextFlags) {
		// To support escaped URLs (like "https:\/\/")
		if (url.startsWith("https:")) {
			// could call replayContext.resolve(url) instead, but this all we need here.
			try {
				url = UsableURIFactory.getInstance(url).toString();
			} catch (URIException ex) {
				// not a valid absolute URL - let it go
			}
		}
		return replayContext.makeReplayURI(url, null, URLStyle.ABSOLUTE);
	}
}
