package org.archive.wayback.replay;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Given {@link WaybackRequest} and {@link CaptureSearchResults},
 * {@code ReplayCaptureSelector} returns {@link CaptureSearchResult} in the
 * order of preference to fulfill the replay request.
 * <p>
 * After setting {@code request} and {@code captures}, the first call to
 * {@link #next()} returns the best capture to fulfill the request (typically
 * the one <em>closest</em> to the requested date). As it may disregard some
 * captures not suitable for the request, this first call to {@code next()} may
 * return {@code null} even though there are one or more captures in
 * {@code captures}.
 * </p>
 * <p>
 * Subsequent call to {@code next()} returns the second best candidate, third
 * best candidate, and so on, until it returns {@code null}, which indicates
 * there's no more captures suitable for the request.
 * </p>
 * <p>
 * {@link #setCaptures(CaptureSearchResults)} may be called to re-initialize it
 * with new set of captures. Next call to {@code next()} will return the best
 * capture within the capture set.
 * </p>
 * <p>
 * Note that {@code ReplayCaptureSelector} evaluates captures' suitability for
 * the request solely on the information available in {@code WaybackRequest} and
 * {@code CaptureSearchResults}. Returned captures may turn out to be unsuitable
 * for the request for other reasons. For example, a capture may be unusable
 * because its record is inaccessible or it is a self-redirect.
 * </p>
 * @see AccessPoint
 */
public interface ReplayCaptureSelector {
	/**
	 * set request information.
	 * <p>
	 * {@code AccessPoint} calls this method just once on each instance.
	 * </p>
	 * @param wbRequest WaybackRequest for the replay request
	 */
	public abstract void setRequest(WaybackRequest wbRequest);

	/**
	 * set a list of captures to select replay candidate from.
	 * <p>
	 * {@code AccessPoint} may call this method more than once with different
	 * instance of {@code CaptureSearchResults} within a request. When this
	 * method is called, next {@code next()} call shall return the best capture
	 * in new {@code CaptureSearchResults}.
	 * </p>
	 * <p>
	 * Implementation can make changes to CaptureSearchResults for bookkeeping
	 * purposes etc.
	 * </p>
	 * @param captures CaptureSearchResults, non-{@code null}
	 */
	public abstract void setCaptures(CaptureSearchResults captures);

	/**
	 * return next best capture.
	 * @return CaptureSearchResult, or {@code null} if there's no more captures
	 *         suitable for the request.
	 */
	public abstract CaptureSearchResult next();
}
