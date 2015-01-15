package org.archive.wayback.resourceindex.cdxserver;

import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthChecker;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.filter.CDXAccessFilter;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Variant of {@link AuthToken} used to pass {@link AccessPoint} reference to
 * {@link AuthChecker} implementations. Primary purpose is to configure
 * {@link CDXAccessFilter} with AccessPoint-dependent information,
 * such as {@link AccessPoint#getFileIncludePrefixes()}.
 * <p>
 * TODO: Considering AuthToken represents a user, it sounds like a bad design to have
 * it carry target context information. It is in fact resulting in counter-intuitive data
 * flow. Refactor the {@link CDXServer} interface.
 * </p>
 * @see EmbeddedCDXServerIndex#createAuthToken(org.archive.wayback.core.WaybackRequest, String)
 * @see WaybackAPAuthChecker
 *
 */
public class APContextAuthToken extends AuthToken {
	final AccessPoint ap;

	public APContextAuthToken(AccessPoint ap) {
		this.ap = ap;
	}

	public AccessPoint getAccessPoint() {
		return ap;
	}
}
