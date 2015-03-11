package org.archive.wayback.memento;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.archive.util.ArchiveUtils;
import org.archive.wayback.ReplayURIConverter;
import org.archive.wayback.ReplayURIConverter.URLStyle;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.partition.NotableResultExtractor;
import org.archive.wayback.util.ObjectFilterIterator;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.webapp.AccessPoint;

public class MementoUtils implements MementoConstants {

	private static final ThreadLocal<SimpleDateFormat> TL_LINK_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat df = new SimpleDateFormat(HTTP_LINK_DATE_FORMAT, Locale.ENGLISH);
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			return df;
		}
	};
	
	protected static String formatLinkDate(Date date) {
		return TL_LINK_DATE_FORMAT.get().format(date);
	}
	
	/**
	 * format <i>label</i>{@code ="}<i>date in HTTP date format</i>{@code "}.
	 * @param label text representing type of date, ex: {@code "from"}.
	 * @param date date
	 */
	protected static String captureDate(String label, Date date) {
		return label + "=\"" + formatLinkDate(date) + "\"";
	}

	public static void printTimemapResponse(CaptureSearchResults results,
			WaybackRequest wbRequest, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/link-format");
		printLinkTimemap(results, wbRequest, response.getWriter());
	}

	public static void printLinkTimemap(CaptureSearchResults results,
			WaybackRequest wbr, PrintWriter pw) {
		Date first = results.getFirstResultDate();
		Date last = results.getLastResultDate();
		AccessPoint ap = wbr.getAccessPoint();

		String requestUrl = wbr.getRequestUrl();
		// ludab nov30 2012

		String pagedate = wbr.get(PAGE_STARTS);
		if (pagedate == null) {
			pagedate = "";
		} else {
			pagedate = pagedate + "/";
		}
		// end

		pw.print(makeLink(requestUrl, ORIGINAL));
		pw.println(",");
		// ludab nov 30 2012

		// pw.print(makeLink(getTimemapUrl(ap,FORMAT_LINK,requestUrl),
		// TIMEMAP,APPLICATION_LINK_FORMAT));
		pw.print(makeLink(
			getTimemapDateUrl(ap, FORMAT_LINK, pagedate, requestUrl), "self",
			APPLICATION_LINK_FORMAT) + "; " + captureDate("from", first) + "; " + captureDate("until", last));
		// end
		pw.println(",");
		pw.print(makeLink(getTimegateUrl(ap, requestUrl), TIMEGATE));
		pw.println(",");

		if (first.compareTo(last) == 0) {
			// special handling of single result:
			CaptureSearchResult result = results.getResults().get(0);
			pw.print(makeLink(ap, result.getOriginalUrl(), FIRST_LAST_MEMENTO,
				result));
		} else {
			List<CaptureSearchResult> lr = results.getResults();
			int count = lr.size();
			String rel;
			for (int i = 0; i < count; i++) {
				CaptureSearchResult result = lr.get(i);
				if (i == 0) {
					rel = FIRST_MEMENTO;
				} else if (i == count - 1) {
					pw.println(",");
					rel = LAST_MEMENTO;
				} else {
					pw.println(",");
					rel = MEMENTO;
				}
				pw.print(makeLink(ap, result.getOriginalUrl(), rel, result));
			}
		}
		// ludab nov 30 2012
		if (results.getMatchingCount() > results.getReturnedCount()) {
			int sec = last.getSeconds() + 1;
			last.setSeconds(sec);
			pw.println(",");
			pw.print(makeLink(
				getTimemapDateUrl(ap, FORMAT_LINK,
					ArchiveUtils.get14DigitDate(last) + "/", requestUrl),
				TIMEMAP, APPLICATION_LINK_FORMAT) +
				"; " + captureDate("from", last));
		}
		// end

		pw.flush();
	}

	/**
	 * Add {@code Link} header value. Includes: {@code first}, {@code prev},
	 * {@code next}, {@code last}, {@code original} (if {@code includeOriginal}
	 * is {@code true}), and {@code timegate} (if {@code includeTimegateLink} is
	 * {@code true}).
	 * 
	 */
	public static String generateMementoLinkHeaders(
			CaptureSearchResults results, WaybackRequest wbr,
			boolean includeTimegateLink, boolean includeOriginalLink) {
		NotableResultExtractor nre = getNotableResults(results);
		CaptureSearchResult first = nre.getFirst();
		CaptureSearchResult prev = nre.getPrev();
		CaptureSearchResult closest = nre.getClosest();
		CaptureSearchResult next = nre.getNext();
		CaptureSearchResult last = nre.getLast();
		ArrayList<String> rels = new ArrayList<String>();

		AccessPoint ap = wbr.getAccessPoint();

		String requestUrl = wbr.getRequestUrl();

		// add generics:
		// rels.add(makeLink(getTimebundleUrl(ap, requestUrl), TIMEBUNDLE));
		if (includeOriginalLink) {
			rels.add(makeLink(requestUrl, ORIGINAL));
		}

		rels.add(makeLink(getTimemapUrl(ap, FORMAT_LINK, requestUrl), TIMEMAP,
			APPLICATION_LINK_FORMAT));

		// Spec says not to include timegate link for timegate
		if (includeTimegateLink) {
			rels.add(makeLink(getTimegateUrl(ap, requestUrl), TIMEGATE));
		}

		// add first/prev/next/last:
		if (first == last) {
			// only one capture.. are we sure we want the "actual" memento here?
			rels.add(makeLink(ap, requestUrl, FIRST_LAST_MEMENTO, first));
		} else {
			if (first == closest) {
				// no previous:
				rels.add(makeLink(ap, requestUrl, FIRST_MEMENTO, first));
				if (next == last) {
					rels.add(makeLink(ap, requestUrl, NEXT_LAST_MEMENTO, last));
				} else {
					rels.add(makeLink(ap, requestUrl, NEXT_MEMENTO, next));
					rels.add(makeLink(ap, requestUrl, LAST_MEMENTO, last));
				}
			} else if (last == closest) {
				// no next:
				rels.add(makeLink(ap, requestUrl, LAST_MEMENTO, last));
				if (prev == first) {
					rels.add(makeLink(ap, requestUrl, PREV_FIRST_MEMENTO, first));
				} else {
					rels.add(makeLink(ap, requestUrl, FIRST_MEMENTO, first));
					rels.add(makeLink(ap, requestUrl, PREV_MEMENTO, prev));
				}
			} else {
				// somewhere in the middle:

				if (prev == first) {
					rels.add(makeLink(ap, requestUrl, PREV_FIRST_MEMENTO, first));
				} else {
					// add both prev and first:
					rels.add(makeLink(ap, requestUrl, FIRST_MEMENTO, first));
					rels.add(makeLink(ap, requestUrl, PREV_MEMENTO, prev));
				}
				// add "actual" memento:
				rels.add(makeLink(ap, requestUrl, MEMENTO, closest));
				if (next == last) {
					rels.add(makeLink(ap, requestUrl, NEXT_LAST_MEMENTO, last));
				} else {
					rels.add(makeLink(ap, requestUrl, NEXT_MEMENTO, next));
					rels.add(makeLink(ap, requestUrl, LAST_MEMENTO, last));
				}
			}
		}
		return StringFormatter.join(", ", rels.toArray(a));
	}

	static String[] a = new String[0];

	public static void addVaryHeader(HttpServletResponse response) {
		response.setHeader(VARY, NEGOTIATE_DATETIME);
	}

	public static boolean hasLinkHeader(HttpServletResponse response) {
		// HRM.. Are we sure it's *our* Link header, and has the rel="original"?
		return response.containsHeader(LINK);
	}

	/**
	 * Add {@code Link} header with just {@code original} relation link
	 * {@code url}.
	 * @param response
	 * @param url
	 */
	public static void addOrigHeader(HttpServletResponse response, String url) {
		response.setHeader(LINK, makeLink(url, ORIGINAL));
	}

	public static void addDoNotNegotiateHeader(HttpServletResponse response) {
		// New Non-Negotiate header
		// Link: <http://mementoweb.org/terms/donotnegotiate">; rel="type" 
		response.setHeader(LINK,
			makeLink("http://mementoweb.org/terms/donotnegotiate", "type"));
	}

	public static void addOrigHeader(HttpServletResponse response,
			WaybackRequest wbr) {
		addOrigHeader(response, wbr.getRequestUrl());
	}

	public static String makeOrigHeader(String url) {
		return makeLink(url, ORIGINAL);
	}

	/**
	 * Add {@code Memento-Datetime} header, and full {@code Link} header if
	 * {@code wbr} is not a Memento Timegate request.
	 * @param response
	 * @param results
	 * @param result
	 * @param wbr
	 * @deprecated 1.8.1 2014-09-12 use
	 *             {@link #addMementoDatetimeHeader(HttpServletResponse, CaptureSearchResult)}
	 *             and
	 *             {@link #addLinkHeader(HttpServletResponse, CaptureSearchResults, WaybackRequest, boolean, boolean)}
	 */
	public static void addMementoHeaders(HttpServletResponse response,
			CaptureSearchResults results, CaptureSearchResult result,
			WaybackRequest wbr) {
		response.setHeader(MEMENTO_DATETIME, formatLinkDate(results.getClosest().getCaptureDate()));

		if (!wbr.isMementoTimegate()) {
			response.setHeader(LINK,
				generateMementoLinkHeaders(results, wbr, true, true));
		}
	}

	/**
	 * Add {@code Memento-Datetime} header.
	 * @param response HttpServletResponse
	 * @param result Capture whose timestamp is used
	 */
	public static void addMementoDatetimeHeader(HttpServletResponse response,
			CaptureSearchResult result) {
		response.setHeader(MEMENTO_DATETIME, formatLinkDate(result.getCaptureDate()));
	}

	/**
	 * Add {@code Link} header.
	 * @param response HttpServletResponse
	 * @param results CaptureSearchResults for generating first/last and
	 *        prev/next relation links
	 * @param wbr WaybackRequest for accessing {@link AccessPoint}
	 * @param includeTimegateLink whether {@code timegate} relation link is
	 *        included ({@code false} for Timegate response, {@code true} for
	 *        Memento response)
	 * @param includeOriginalLink whether {@code original} relation link is
	 *        included (usually {@code true})
	 */
	public static void addLinkHeader(HttpServletResponse response,
			CaptureSearchResults results, WaybackRequest wbr,
			boolean includeTimegateLink, boolean includeOriginalLink) {
		response.setHeader(
			LINK,
			generateMementoLinkHeaders(results, wbr, includeTimegateLink,
				includeOriginalLink));
	}

	/**
	 * Add {@code Vary: accept-datetime} header and {@code Link} header for
	 * timegate response. See
	 * {@link #generateMementoLinkHeaders(CaptureSearchResults, WaybackRequest, boolean, boolean)}
	 * for details of {@code Link} header.
	 * @param response
	 * @param results
	 * @param wbr
	 * @param includeOriginal
	 */
	public static void addTimegateHeaders(HttpServletResponse response,
			CaptureSearchResults results, WaybackRequest wbr,
			boolean includeOriginal) {
		addVaryHeader(response);
		addLinkHeader(response, results, wbr, false, includeOriginal);
	}

//	private static String getTimegatePrefix(AccessPoint ap) {
//		// if(ap.getClass().isAssignableFrom(MementoAccessPoint.class)) {
//		String prefix = null;
//		if (ap instanceof MementoAccessPoint) {
//			prefix = ((MementoAccessPoint) ap).getTimegatePrefix();
//		}
//		if (prefix == null) {
//			prefix = getProp(ap.getConfigs(), TIMEGATE_PREFIX_CONFIG, null);
//		}
//		// TODO: rationalize...
//		if (prefix == null) {
//			prefix = ap.getReplayPrefix();
//		}
//		return prefix;
//	}

	public static final SimpleDateFormat ACCEPT_DATE_FORMATS[] = {
		new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z"),
		new SimpleDateFormat("E, dd MMM yyyy Z"),
		new SimpleDateFormat("E, dd MMM yyyy") };

	public static Date parseAcceptDateTimeHeader(String datespec) {
		for (SimpleDateFormat format : ACCEPT_DATE_FORMATS) {
			try {
				return format.parse(datespec);
			} catch (ParseException e) {
				// ignore and move on..
			}
		}

		return null;
	}

	public static String getTimegateUrl(AccessPoint ap, String url) {
		StringBuilder sb = new StringBuilder();
		sb.append(getTimeGatePrefix(ap));
		sb.append(url);
		return sb.toString();
	}

	public static String getTimemapUrl(AccessPoint ap, String format, String url) {
		StringBuilder sb = new StringBuilder();
		sb.append(getTimeMapPrefix(ap));
		sb.append(TIMEMAP).append("/").append(format).append("/");
		sb.append(url);
		return sb.toString();
	}

	public static String getTimemapDateUrl(AccessPoint ap, String format,
			String pagestr, String url) {
		StringBuilder sb = new StringBuilder();
		sb.append(getTimeMapPrefix(ap));
		sb.append(TIMEMAP).append("/").append(format).append("/");
		sb.append(pagestr);
		sb.append(url);
		return sb.toString();
	}

	public static String getTimeMapPrefix(AccessPoint ap) {
		return getMementoPrefix(ap) + ap.getQueryPrefix();
	}

	public static String getTimeGatePrefix(AccessPoint ap) {
		return getMementoPrefix(ap) + ap.getReplayPrefix();
	}

	public static String getMementoPrefix(AccessPoint ap) {
		return getProp(ap.getConfigs(), AGGREGATION_PREFIX_CONFIG, "");

//		String prefix = null;
//		if (ap instanceof MementoAccessPoint) {
//			prefix = ((MementoAccessPoint) ap).getTimegatePrefix();
//		}
//		// TODO: rationalize...
//		if (prefix == null) {
//			prefix = getProp(ap.getConfigs(), AGGREGATION_PREFIX_CONFIG, "");
//		}
//		if (prefix == null) {
//			prefix = ap.getQueryPrefix();
//		}
//		return prefix;
	}

	public static int getPageMaxRecord(AccessPoint ap) {
		String mr;
		mr = getProp(ap.getConfigs(), PAGE_MAXRECORDS_CONFIG, "0");
		if (mr == null) {
			mr = "0";
		}
		return new Integer(mr).intValue();
	}

	private static String getProp(Properties p, String name, String deflt) {
		if (p == null) {
			return deflt;
		}
		return p.getProperty(name, deflt);
	}

	private static String makeLink(String url, String rel) {
		return String.format("<%s>; rel=\"%s\"", url, rel);
	}

	private static String makeLink(String url, String rel, String type) {
		return String.format("<%s>; rel=\"%s\"; type=\"%s\"", url, rel, type);
	}

	private static String makeLink(AccessPoint ap, String url, String rel,
			CaptureSearchResult result) {

		Date date = result.getCaptureDate();
		String timestamp = ArchiveUtils.get14DigitDate(date);
		ResultURIConverter uriConverter = ap.getUriConverter();
		final String replayURI;
		if (uriConverter instanceof ReplayURIConverter) {
			// leverage new interface.
			replayURI = ((ReplayURIConverter)uriConverter).makeReplayURI(timestamp, url, null, URLStyle.ABSOLUTE);
		} else {
			replayURI = getMementoPrefix(ap) + uriConverter.makeReplayURI(timestamp, url);
		}
		String httpTime = formatLinkDate(date);

//		return String.format("<%s%s>; rel=\"%s\"; datetime=\"%s\"; status=\"%s\"", prefix, replayURI,
//				rel, httpTime, result.getHttpCode());
		return String.format("<%s>; rel=\"%s\"; datetime=\"%s\"", replayURI,
			rel, httpTime);
	}

	private static NotableResultExtractor getNotableResults(
			CaptureSearchResults r) {
		// eventually, the NotableResultExtractor will be part of the standard
		// ResourceIndex.query() but for now, we'll just do an extra traversal
		// of the whole set of results:

		Iterator<CaptureSearchResult> itr = r.iterator();
		Date want = r.getClosest().getCaptureDate();
		NotableResultExtractor nre = new NotableResultExtractor(want);

		ObjectFilterIterator<CaptureSearchResult> ofi = new ObjectFilterIterator<CaptureSearchResult>(
			itr, nre);
		while (ofi.hasNext()) {
			ofi.next();
		}
		return nre;
	}
}
