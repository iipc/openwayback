package org.archive.wayback.replay;

import java.net.URISyntaxException;

import org.archive.wayback.ReplayURIConverter.URLStyle;

/**
 * {@code ReplayContext} provides {@link ReplayURLTransformer} with information
 * and service related to resource being replayed, and URL projection
 * scheme of access point through which the resource is replayed.
 */
public interface ReplayContext {
	/**
	 * Resolve non-full URL by the URL of resource being replayed.
	 * <p>
	 * TODO: this method is implemented by ParseContext, not ReplayParseContext.
	 * Probably this method and setter/getter for {@code baseUrl} are
	 * better be moved to ReplayParseContext. Looking at the code it makes sense.
	 * </p>
	 * @param url URL to resolve
	 * @return full URL
	 * @throws URISyntaxException if {@code url} is not valid URI
	 */
	public String resolve(String url) throws URISyntaxException;

	/**
	 * Build a URL for replaying {@code url} in replay context {@code flags},
	 * in URL style {@code urlStyle}.
	 * @param url URL of resource to be replayed (absolute URL)
	 * @param flags replay context flags (may be {@code null})
	 * @param urlStyle desired style of URL to be constructed
	 * @return projected replay URL
	 */
	public String makeReplayURI(String url, String flags, URLStyle urlStyle);
}