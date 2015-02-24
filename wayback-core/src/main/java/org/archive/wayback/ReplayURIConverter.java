/**
 *
 */
package org.archive.wayback;

import org.archive.wayback.replay.ReplayURLTransformer;
import org.archive.wayback.webapp.AccessPoint;
import org.archive.wayback.webapp.AccessPointAware;

/**
 * ReplayURIConverter offers service for constructing a URL for replaying
 * archived captures, in specific replay projection space.
 * <p>
 * This interface should be distinguished from a service interface for
 * rewriting URIs in a resource being replayed. An instance of implementing
 * class is typically configured with a Wayback access point, to define
 * its projection scheme, i.e. how URI-R mapped onto a URL-M, in Memento terms.
 * </p>
 * <p>
 * Typical implementation class also implement {@link AccessPointAware}
 * interface to configure itself with base URL of {@link AccessPoint},
 * and {@link ReplayURLTransformer} to provide sophisticated URL translation
 * service for the projection scheme.
 * </p>
 * <p>
 * TODO: This interface is defined as an extension of ResultURIConverter because
 * adding new method to ResultURIConverter can break existing customized
 * deployment. This interface shall be merged into ResultURIConverter in
 * the next major release. {@code ResultURIConverter#makeReplayURI(String, String)}
 * is still useful for simple cases.
 * </p>
 */
public interface ReplayURIConverter extends ResultURIConverter {
	public enum URLStyle {
		/**
		 * So-called absolute URL; with protocol, fully-qualified
		 * server name:port, and path.
		 */
		ABSOLUTE,
		/**
		 * URL starting with "//" followed by fully-qualified
		 * server name:port, and path
		 */
		PROTOCOL_RELATIVE,
		/**
		 * Full path only, no protocol and server-name parts.
		 */
		SERVER_RELATIVE
	}

	/**
	 * Build a projected URL for replaying URL at {@code datespec}
	 * in replay context {@code flags}, in style given.
	 * <p>
	 * Implementation may ignore {@code style} parameter if projection
	 * scheme mandates particular style.
	 * </p>
	 * @param datespec archive time in timestamp format
	 * (shall not include context flags)
	 * @param url URL of resource to be replayed (full URL)
	 * @param flags replay context flags (may be {@code null})
	 * @param urlStyle desired style of URL to be constructed
	 * @return projected replay URL
	 */
	public String makeReplayURI(String datespec, String url, String flags,
			URLStyle urlStyle);

	/**
	 * Return URL-rewrite service for this replay projection scheme.
	 * @return ReplayURLTransformer implementation, or {@code null}.
	 */
	public ReplayURLTransformer getURLTransformer();
}
