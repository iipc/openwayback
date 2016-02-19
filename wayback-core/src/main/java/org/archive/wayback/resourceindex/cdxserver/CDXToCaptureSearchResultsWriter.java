package org.archive.wayback.resourceindex.cdxserver;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang.math.NumberUtils;
import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthChecker;
import org.archive.format.cdx.CDXLine;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.filterfactory.ExclusionCaptureFilterGroup;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.AccessPoint;

/**
 * {@link CDXToSearchResultWriter} for producing {@link CaptureSearchResults}.
 * <p>Also resolves revisits and sets closest.</p>
 */
public class CDXToCaptureSearchResultsWriter extends CDXToSearchResultWriter {

	public final static String REVISIT_VALUE = "warc/revisit";

	protected CaptureSearchResults results = null;

	protected String targetTimestamp;
	protected int flip = 1;
	protected boolean done = false;
	protected CaptureSearchResult closest = null;
	protected SelfRedirectFilter selfRedirFilter = null;
	protected ExclusionFilter exclusionFilter = null;

	protected CaptureSearchResult prevResult = null;
	protected CDXLine prevLine = null;

	protected HashMap<String, CaptureSearchResult> digestToOriginal;
	protected HashMap<String, LinkedList<CaptureSearchResult>> digestToRevisits;

	protected boolean resolveRevisits = false;
	protected boolean seekSingleCapture = false;
	protected boolean isReverse = false;

	protected String preferContains = null;

	// tentative
	protected boolean includeBlockedCaptures = false;

	protected boolean excludeCaptureWithUserInfo = true;
	
	/**
	 * Initialize with behavior options.
	 * <p>
	 * This class generates {@link CaptureSearchResult} in chronological
	 * order, even when {@link CDXQuery#isReverse()} is {@code true}.
	 * </p>
	 */
	public CDXToCaptureSearchResultsWriter() {
	}

	/**
	 * Whether to block urls with personal info in them
	 * @param excludeCaptureWithUserInfo
	 */
	public void setExcludeCaptureWithUserInfo(boolean excludeCaptureWithUserInfo) {
		this.excludeCaptureWithUserInfo = excludeCaptureWithUserInfo;
	}
	
	/**
	 * Whether to resolve revisit captures
	 * @param resolveRevisits
	 */
	public void setResolveRevisits(boolean resolveRevisits) {
		this.resolveRevisits = resolveRevisits;
	}

	/**
	 * Whether just one capture is wanted.
	 * Only effective when {@code resolveRevisits} is also {@code true}.
	 * @param seekSingleCapture
	 */
	public void setSeekSingleCapture(boolean seekSingleCapture) {
		this.seekSingleCapture = seekSingleCapture;
	}

	/**
	 * Whether CDXes are fed in reverse order.
	 * @param isReverse
	 * @see CDXQuery#isReverse()
	 */
	public void setReverse(boolean isReverse) {
		this.isReverse = isReverse;
	}

	/**
	 * Preferred archive filename substring. If
	 * non-{@code null}, It picks capture in the archive with a given substring
	 * in its filename, out of multiple captures of the same timestamp, original
	 * URL, length and offset (if any).
	 * <p>
	 * Note: This is specifically intended for
	 * choosing one out of two copies of the identical capture record in different
	 * storage locations.  For example, If WARCs in staging area are made available
	 * for replay through secondary index, there may be a period where one capture
	 * is indexed in both main and secondary index, with different {@code filename}
	 * field. If {@code preferContains} is set, CDX line that has {@code preferContains}
	 * as substring in {@code filename} will be picked over others that does not.
	 * It can be used, for example, to put higher preference on the archive in primary
	 * storage area.
	 * </p>
	 * @param preferContains
	 */
	public void setPreferContains(String preferContains) {
		this.preferContains = preferContains;
	}

	/**
	 * Initialize with CDXQuery and behavior options.
	 * @param query
	 * @param resolveRevisits
	 * @param seekSingleCapture
	 * @param preferContains
	 * @deprecated 2015-09-04 Use {@link #CDXToCaptureSearchResultsWriter(boolean, boolean, String)}
	 */
	public CDXToCaptureSearchResultsWriter(CDXQuery query,
			boolean resolveRevisits, boolean seekSingleCapture,
			String preferContains) {
		super(query);

		this.resolveRevisits = resolveRevisits;
		this.seekSingleCapture = seekSingleCapture;
		this.isReverse = query.isReverse();
		this.preferContains = preferContains;
	}

	public void setTargetTimestamp(String timestamp) {
		targetTimestamp = timestamp;

		if (isReverse) {
			flip = -1;
		}
	}

	@Override
	public void begin() {
		results = new CaptureSearchResults();

		if (resolveRevisits) {
			if (isReverse) {
				digestToRevisits = new HashMap<String, LinkedList<CaptureSearchResult>>();
			} else {
				digestToOriginal = new HashMap<String, CaptureSearchResult>();
			}
		}
	}

	/**
	 * perform exclusion filtering
	 * @param result true if result should be excluded, false otherwise
	 * @return
	 */
	protected boolean performExclusionFiltering(CaptureSearchResult result) {
		if (exclusionFilter != null) {
			if (exclusionFilter.filterObject(result) != ObjectFilter.FILTER_INCLUDE) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int writeLine(CDXLine line) {
		String timestamp = line.getTimestamp();
		String originalUrl = line.getOriginalUrl();

		if ((prevResult != null) && (preferContains != null) &&
				prevResult.getCaptureTimestamp().equals(timestamp) &&
				prevResult.getOriginalUrl().equals(originalUrl) &&
				prevLine.getLength().equals(line.getLength()) &&
				prevLine.getOffset().equals(line.getOffset())) {

			String currFile = line.getFilename();
			String prevFile = prevLine.getFilename();

			if (currFile.contains(preferContains) &&
					!prevFile.contains(preferContains)) {
				prevResult.setFile(currFile);
			}

			return 0;
		}

		FastCaptureSearchResult result = new FastCaptureSearchResult();

		result.setUrlKey(line.getUrlKey());
		result.setCaptureTimestamp(timestamp);
		result.setOriginalUrl(originalUrl);

		if (excludeCaptureWithUserInfo) {
			// Special case: filter out captures that have userinfo
			boolean hasUserInfo = (UrlOperations.urlToUserInfo(result
				.getOriginalUrl()) != null);

			if (hasUserInfo) {
				return 0;
			}
		}

		result.setRedirectUrl(line.getRedirect());
		result.setHttpCode(line.getStatusCode());

		if (selfRedirFilter != null &&
				!result.getRedirectUrl().equals(CDXLine.EMPTY_VALUE)) {
			if (selfRedirFilter.filterObject(result) != ObjectFilter.FILTER_INCLUDE) {
				return 0;
			}
		}

		// make these fields available to exclusionFilter. it may also modify some fields
		// (typically robotflags field).
		result.setMimeType(line.getMimeType());
		result.setDigest(line.getDigest());
		result.setFile(line.getFilename());
		// ugly - move this check to FastCaptureSearchResult#setRobotFlags
		if (!"-".equals(line.getRobotFlags()))
			result.setRobotFlags(line.getRobotFlags());

		if (performExclusionFiltering(result)) {
			return 0;
		}
		
		result.setOffset(NumberUtils.toLong(line.getOffset(), -1));
		result.setCompressedLength(NumberUtils.toLong(line.getLength(), -1));

		boolean isRevisit = false;

		if (resolveRevisits) {
			isRevisit = result.getFile().equals(CDXLine.EMPTY_VALUE) ||
					result.getMimeType().equals(REVISIT_VALUE);

			String digest = result.getDigest();

			if (isRevisit) {
				if (!isReverse) {
					CaptureSearchResult payload = digestToOriginal.get(digest);
					if (payload != null) {
						result.flagDuplicateDigest(payload);
					} else {
						result.flagDuplicateDigest();
					}
				} else {
					LinkedList<CaptureSearchResult> revisits = digestToRevisits
							.get(digest);
					if (revisits == null) {
						revisits = new LinkedList<CaptureSearchResult>();
						digestToRevisits.put(digest, revisits);
					}
					revisits.add(result);
				}
			} else {
				if (!isReverse) {
					digestToOriginal.put(digest, result);
				} else {
					LinkedList<CaptureSearchResult> revisits = digestToRevisits
							.remove(digest);
					if (revisits != null) {
						for (CaptureSearchResult revisit : revisits) {
							revisit.flagDuplicateDigest(result);
						}
					}
				}
			}
		}

//		String payloadFile = line.getField(RevisitResolver.origfilename);
//
//		if (!payloadFile.equals(CDXLine.EMPTY_VALUE)) {
//			FastCaptureSearchResult payload = new FastCaptureSearchResult();
//			payload.setFile(payloadFile);
//			payload.setOffset(NumberUtils.toLong(line.getField(RevisitResolver.origoffset), -1));
//			payload.setCompressedLength(NumberUtils.toLong(line.getField(RevisitResolver.origlength), -1));
//			result.flagDuplicateDigest(payload);
//		}

		// Drop soft-blocked captures after resolving revisits. They are excluded
		// from regular replay, but available as the original of revisits.
		// It is disabled when AccessPoint is looking up the original for a
		// URL-agnostic revisit (indicated by includeBlockedCaptures flag).
		if (!includeBlockedCaptures && result.isRobotFlagSet(CaptureSearchResult.CAPTURE_ROBOT_BLOCKED)) {
			return 0;
		}

		if ((targetTimestamp != null) && (closest == null)) {
			closest = determineClosest(result);
		}

		results.addSearchResult(result, !isReverse);
		prevResult = result;
		prevLine = line;

		// Short circuit the load if seeking single capture
		if (seekSingleCapture && resolveRevisits) {
			if (closest != null) {
				// If not a revisit, we're done
				if (!isRevisit) {
					done = true;
					// Else make sure the revisit is resolved
				} else if (result.getDuplicatePayload() != null) {
					done = true;
				}
			}
		}

		return 1;
	}

	@Override
	public boolean isAborted() {
		return done;
	}

	protected CaptureSearchResult determineClosest(
			CaptureSearchResult nextResult) {
		int compare = targetTimestamp.compareTo(nextResult
			.getCaptureTimestamp()) * flip;

		if (compare == 0) {
			return nextResult;
		} else if (compare > 0) {
			// Too early to tell
			return null;
		}

		// First result that is greater/less than target
		if (results.isEmpty()) {
			return nextResult;
		}

		CaptureSearchResult lastResult = getLastAdded();

		// Now compare date diff
		long nextTime = nextResult.getCaptureDate().getTime();
		long lastTime = lastResult.getCaptureDate().getTime();

		long targetTime = Timestamp.parseAfter(targetTimestamp).getDate()
				.getTime();

		if (Math.abs(nextTime - targetTime) < Math.abs(lastTime - targetTime)) {
			return nextResult;
		} else {
			return lastResult;
		}
	}

	public void end() {
		results.setClosest(this.getClosest());
		results.setReturnedCount(results.getResults().size());
		results.setMatchingCount(results.getResults().size());
	}

	public CaptureSearchResult getClosest() {
		if (closest != null) {
			return closest;
		}

		if (!results.isEmpty()) {
			// If no target timestamp, always return the latest capture,
			// otherwise first or last based on reverse state
			if (targetTimestamp != null) {
				return getLastAdded();
			} else {
				return results.getResults().getLast();
			}
		}

		return null;
	}

	protected CaptureSearchResult getLastAdded() {
		if (!isReverse) {
			return results.getResults().getLast();
		} else {
			return results.getResults().getFirst();
		}
	}

	@Override
	public CaptureSearchResults getSearchResults() {
		return results;
	}

	public SelfRedirectFilter getSelfRedirFilter() {
		return selfRedirFilter;
	}

	public void setSelfRedirFilter(SelfRedirectFilter selfRedirFilter) {
		this.selfRedirFilter = selfRedirFilter;
	}

	@Deprecated
	public ExclusionFilter getExclusionFilter() {
		return exclusionFilter;
	}
	/**
	 * If non-{@code null}, the filter will be applied before revisit
	 * resolution.
	 * <p>Note: there is no class using this property in baseline Wayback.
	 * You need to write a custom class to utilize this property.
	 * See {@link CDXServer} and {@link LocalResourceIndex}
	 * for other ways of configuring exclusion filters.
	 * </p>
	 * <p>
	 * This method is deprecated because this can run exclusion after
	 * timestamp deduplication, which results in undesirable capture
	 * search results. Exclusion should happen in regular CDXServer
	 * pipeline. This method was necessary to implement collection sensitive
	 * exclusion filter. New exclusion filter factory addresses such needs
	 * in ordinary CDX filtering pipeline.
	 * </p>
	 * @param exclusionFilter
	 * @see CDXServer
	 * @see LocalResourceIndex
	 * @see AuthChecker#createAccessFilter(org.archive.cdxserver.auth.AuthToken)
	 * @see ExclusionCaptureFilterGroup#ExclusionCaptureFilterGroup(org.archive.wayback.core.WaybackRequest, org.archive.wayback.UrlCanonicalizer)
	 * @deprecated 2014-11-10 Use new implementation {@link AccessPoint#setExclusionFactory(org.archive.wayback.accesscontrol.ExclusionFilterFactory)}
	 */
	public void setExclusionFilter(ExclusionFilter exclusionFilter) {
		this.exclusionFilter = exclusionFilter;
	}

	public boolean isIncludeBlockedCaptures() {
		return includeBlockedCaptures;
	}
	/**
	 * set to {@code true} if blocked captures are to be included
	 * in the result.
	 * <p>This is a tentative property and specifically intended for
	 * looking up revisit original for URL-agnostic revisits. May change
	 * in the future.</p>
	 * @param includeBlockedCaptures
	 */
	public void setIncludeBlockedCaptures(boolean includeBlockedCaptures) {
		this.includeBlockedCaptures = includeBlockedCaptures;
	}

}
