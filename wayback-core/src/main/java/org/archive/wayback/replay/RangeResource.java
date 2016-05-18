/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual
 *  contributors.
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.replay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.RangeNotSatisfiableException;

import com.google.common.io.ByteStreams;

/**
 * RangeResource decorates Resource to render partial content response
 * to range request.
 *
 * After constructing with wrapped Resource and requested range,
 * call {@link #parseRange()} to prepare internal state for rendering.
 * The method will throw {@link RangeNotSatisfiableException} if the requested
 * byte ranges cannot be replayed with the base Resource.
 *
 * RangeResource currently only works with:
 * <ul>
 * <li>A single-range request
 * <li>A 200 capture with {@code Content-Length}, or a valid 206 capture
 * with single range (i.e. no support for {@code multipart/byteranges})
 * </ul>
 * @see HttpHeaderOperation#parseRanges(String)
 */
public class RangeResource extends Resource {
	private Resource origResource;
	private long[][] requestedRanges;
	private long outputLength;
	private Map<String, String> httpHeaders;

	/**
	 * Initialize with base Resource and requested ranges.
	 * @param origResource base resource
	 * @param requestedRanges requested range
	 * @see HttpHeaderOperation#parseRanges(String)
	 */
	public RangeResource(Resource origResource, long[][] requestedRanges) {
		this.origResource = origResource;
		this.requestedRanges = requestedRanges;
	}

	/**
	 * Prepare response for requested range.
	 * @throws RangeNotSatisfiableException base Resource either does not have
	 * enough data to fulfill the request, or have invalid header field.
	 */
	public void parseRange() throws RangeNotSatisfiableException, IOException {
		if (requestedRanges.length > 1) {
			throw new RangeNotSatisfiableException(origResource, requestedRanges,
				"Multiple ranges are not supported yet");
		}
		final long[] firstRange = requestedRanges[0];
		long start = firstRange[0];
		long stop = firstRange[1];

		long[] availRange = availableRange();
		if (availRange == null) {
			throw new RangeNotSatisfiableException(origResource, requestedRanges,
				"available range cannot be determined");
		}
		if (start < 0) {
			// tail range (-N) - translate to absolute positions
			start = availRange[2] + start;
			stop = availRange[2];
		}
		if (stop < 0) {
			// M-
			stop = availRange[2];
		}
		if (start < availRange[0] || stop > availRange[1]) {
			// requested range is not satisfiable with content available
			// in the resource.
			// TODO: if availRange[1] == file length, stop > availRange[1]
			// is okay. We just replay start..availRange[1]. Guessing it
			// must be extremely rare to have such capture.
			throw new RangeNotSatisfiableException(origResource, requestedRanges,
				"requested range is not available in this capture");
		}
		if (start > availRange[0]) {
			origResource.skip(start - availRange[0]);
		}
		outputLength = stop - start;
		setInputStream(ByteStreams.limit(origResource, outputLength));

		httpHeaders = new HashMap<String, String>();
		httpHeaders.putAll(origResource.getHttpHeaders());
		// TODO: Add Content-Range
		String contentRange = String.format("bytes %d-%d/%d", start,
			stop - 1, availRange[2]);
		HttpHeaderOperation.replaceHeader(httpHeaders,
			HttpHeaderOperation.HTTP_CONTENT_RANGE_HEADER, contentRange);
		HttpHeaderOperation
			.replaceHeader(httpHeaders, HttpHeaderOperation.HTTP_LENGTH_HEADER,
				Long.toString(stop - start));
	}

	/**
	 * Look at resource HTTP header fields and determine the byte range available.
	 * @return array with three long values:
	 * <ol>
	 * <li><em>offset of first available byte</em>,
	 * <li><em>offset of last available byte</em> + 1,
	 * <li><em>instance-length</em>
	 * </ol>
	 * Note this method returns {@code null} for {@code multipart/byteranges}
	 * response.
	 */
	protected long[] availableRange() {
		// for revisit capture, HTTP headers comes from revisiting resource.
		// it should be okay to assume revisited resource has exactly the same
		// Content-Range header field.
		Map<String, String> respHeaders = origResource.getHttpHeaders();
		long[] contentRange = HttpHeaderOperation
			.getContentRange(respHeaders);
		long availStart, availStop, contentLength;
		if (contentRange == null) {
			// regular 200 response (or Content-Range is invalid)
			String clen = HttpHeaderOperation.getContentLength(respHeaders);
			if (clen != null) {
				try {
					contentLength = Long.parseLong(clen);
				} catch (NumberFormatException ex) {
					return null;
				}
			} else {
				// no Content-Length - probably chunked encoded -
				// not supported yet (TODO: determine length by
				// actually reading?)
				return null;
			}
			availStart = 0;
			availStop = contentLength;
		} else {
			// 206, or perhaps 416 response
			if (contentRange[0] < 0) {
				// 416 (both [0] and [1] are -1 for */instance-length)
				return null;
			}
			availStart = contentRange[0];
			availStop = contentRange[1];
			contentLength = contentRange[2];
			// TODO: need support for M-N/*?
			if (contentLength < 0)
				return null;
			// sanity check
			if (availStop <= availStart) return null;
			if (contentLength < availStop) return null;
		}
		return new long[] { availStart, availStop, contentLength };
	}

	@Override
	public void close() throws IOException {
		if (origResource != null) {
			origResource.close();
			origResource = null;
		}
	}

	@Override
	public Map<String, String> getHttpHeaders() {
		return httpHeaders;
	}

	@Override
	public long getRecordLength() {
		return origResource.getRecordLength();
	}

	@Override
	public int getStatusCode() {
		return HttpServletResponse.SC_PARTIAL_CONTENT;
	}
}