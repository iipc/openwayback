package org.archive.wayback.replay;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadContentException;

/**
 * rewrite https link to http in http header (redirect)
 * @author lam
 */
public class HttpsRedirectRewriteReplayRendeder extends
		TransparentReplayRenderer {

	private final static long NOCACHE_THRESHOLD = 100000000L;

	private final static String NOCACHE_HEADER_NAME = "X-Accel-Buffering";
	private final static String NOCACHE_HEADER_VALUE = "no";

	private final static int BUFFER_SIZE = 4096;

	private HttpHeaderProcessor hhp;

	private static final Logger LOGGER = Logger
			.getLogger(HttpsRedirectRewriteReplayRendeder.class.getName());

	public HttpsRedirectRewriteReplayRendeder(
			HttpHeaderProcessor httpHeaderProcessor) {
		super(httpHeaderProcessor);
		hhp = httpHeaderProcessor;
	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource, ResultURIConverter uriConverter,
			CaptureSearchResults results) throws ServletException, IOException,
			BadContentException {

		HttpHeaderOperation.copyHTTPMessageHeader(httpHeadersResource,
				httpResponse);

		// modif BnF : handle session timestamps in redirection

		Map<String, String> headers = HttpHeaderOperation.processHeaders(
				httpHeadersResource, result, uriConverter, hhp);

		if (headers.containsKey("Location") || headers.containsKey("location")) {

			String locationLabel = null;
			if (headers.containsKey("Location")) {
				locationLabel = "Location";
			}
			if (headers.containsKey("location")) {
				locationLabel = "location";
			}

			// modif klm : handle timesteamp if is a redirect

			// we are setting the old session replay timestamp for the new one

			// KLM : handle https redirection case
			if (headers.get(locationLabel).startsWith("https")) {
				String location = headers.get(locationLabel);
				location = "http" + location.substring(5);

				// String requestURL = httpRequest.getRequestURL().toString();
				// TEST KLM
				String requestURL = wbRequest.getRequestUrl();

				// only if the redirection is a https auto redicretion (the same
				// URL but with started with https)
				if (location.equals(requestURL)) {

					headers.put(locationLabel, location);

					CaptureSearchResult prev = null;
					CaptureSearchResult next = null;
					String exactDateStr = wbRequest.getReplayTimestamp();

					Iterator<CaptureSearchResult> it = results.iterator();
					while (it.hasNext()) {
						CaptureSearchResult res = it.next();
						String resDateStr = res.getCaptureTimestamp();
						int compared = resDateStr.compareTo(exactDateStr
								.substring(0, resDateStr.length()));
						if (compared < 0) {
							prev = res;
						} else if (compared > 0) {
							if (next == null) {
								next = res;
							}
						}
					}
					// we have now the previous and the next capture
					if (next != null) {
						wbRequest
								.setReplayTimestamp(next.getCaptureTimestamp());
					} else if (prev != null) {
						wbRequest
								.setReplayTimestamp(prev.getCaptureTimestamp());
					} else {
						// else, eg only 1 https redirect result : we keep https
						// header
						headers.put(locationLabel,
								"https" + location.substring(4));
					}

				} else {
					// rewriting redirected url from https to htto
					headers.put(locationLabel, location);
				}
			}
		}

		// original method

		// HACKHACK: getContentLength() may not find the original content length
		// if a HttpHeaderProcessor has mangled it too badly. Should this
		// happen in the HttpHeaderProcessor itself?
		String origLength = HttpHeaderOperation.getContentLength(headers);
		if (origLength != null) {
			headers.put(HttpHeaderOperation.HTTP_LENGTH_HEADER, origLength);

			long contentLength = -1;

			try {
				contentLength = Long.parseLong(origLength);
			} catch (NumberFormatException n) {

			}

			// TODO: Generalize? Don't buffer NOCACHE_THRESHOLD
			if ((contentLength >= NOCACHE_THRESHOLD)) {
				headers.put(NOCACHE_HEADER_NAME, NOCACHE_HEADER_VALUE);
			}
		}

		HttpHeaderOperation.sendHeaders(headers, httpResponse);

		// and copy the raw byte-stream.
		OutputStream os = httpResponse.getOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		long total = 0;
		for (int r = -1; (r = payloadResource.read(buffer, 0, BUFFER_SIZE)) != -1;) {
			os.write(buffer, 0, r);
			total += r;
		}
		if (total == 0) {
			if (headers.size() == 0) {
				// totally empty response
				httpResponse.setContentLength(0);
			}
		}
	}
}
