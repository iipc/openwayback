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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadContentException;
import org.archive.wayback.replay.charset.CharsetDetector;
import org.archive.wayback.replay.charset.StandardCharsetDetector;
import org.archive.wayback.replay.html.ReplayParseContext;

/**
 * {@link ReplayRenderer} for rewriting textual resource with {@link TextDocument}.
 * <p>
 * {@link Resource} is first decoded (if {@code Transfer-Encoding} is applied),
 * and fully loaded into {@code TextDocument} object. Then {@link #updatePage}
 * method is called for actual rewrite. Then updated content is sent to the response.
 * </p>
 * <p>Customization Properties:
 * <ul>
 * <li>{@code jspInserts}: a list of Servlets for annotation inserts. It is
 * {@code updatePage}'s responsibility to perform actual insertion.</li>
 * <li>{@code charsetDetector}: {@link CharsetDetector} for detecting resource's
 * charset. Default is {@link StandardCharsetDetector}.</li>
 * <li>{@code httpHeaderProcessor} (constructor arg): {@link HttpHeaderProcessor}
 * for rewriting resource's HTTP headers.</li>
 * <li>{@code guessedCharsetHeader}: name of response HTTP header for sending
 * out resource's charset detected by {@code charsetDetector}. Default is
 * {@link #GUESSED_CHARSET_HEADER}.</li>
 * <li>{@code pageURIConverterFactory}: You can use different URI conversion
 * just for {@code TextDocument} and {#updatePage} by setting this property 
 * to non-{@code null} {@code ContextResultURIConverterFactory}. It does not
 * affect URI conversion for HTTP headers.</li>
 * </ul>
 * </p>
 * @author brad
 */
public abstract class TextReplayRenderer implements ReplayRenderer {

	private static final Logger LOGGER = Logger
			.getLogger(TextReplayRenderer.class.getName());

	public static String GUESSED_CHARSET_HEADER = "X-Archive-Guessed-Charset";
	
	public static String ORIG_ENCODING = "X-Archive-Orig-Encoding";

	protected String overrideContentMimeType = null;

	private String guessedCharsetHeader = GUESSED_CHARSET_HEADER;
	private List<String> jspInserts = null;
	private HttpHeaderProcessor httpHeaderProcessor;
	private CharsetDetector charsetDetector = new StandardCharsetDetector();

	public TextReplayRenderer(HttpHeaderProcessor httpHeaderProcessor) {
		this.httpHeaderProcessor = httpHeaderProcessor;
	}

	/**
	 * legacy implementation point. Override this method to implement actual
	 * rewrites on {@code page} content.
	 * @param page
	 * @param httpRequest
	 * @param httpResponse
	 * @param wbRequest
	 * @param result
	 * @param resource
	 * @param uriConverter
	 * @param results
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void updatePage(TextDocument page,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
					throws ServletException, IOException {
		// no op
	}

	protected void updatePage(TextDocument page,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			ReplayParseContext context, Resource resource,
			CaptureSearchResults results)
			throws ServletException, IOException {
		updatePage(page, httpRequest, httpResponse, context.getWaybackRequest(),
			context.getCaptureSearchResult(), resource, context.getURIConverter(), results);
	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
					throws ServletException, IOException, BadContentException {
		renderResource(httpRequest, httpResponse, wbRequest, result, resource, resource, uriConverter, results);
	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource, ResultURIConverter uriConverter,
			CaptureSearchResults results) throws ServletException,
			IOException, BadContentException {

		// Decode resource (such as if gzip encoded)
		Resource decodedResource = decodeResource(httpHeadersResource, payloadResource);
		
		ReplayParseContext context = ReplayParseContext.create(uriConverter, wbRequest, null, result, false);

		HttpHeaderOperation.copyHTTPMessageHeader(httpHeadersResource, httpResponse);

		Map<String,String> headers = HttpHeaderOperation.processHeaders(
				httpHeadersResource, context, httpHeaderProcessor);

		String charSet = charsetDetector.getCharset(httpHeadersResource,
				decodedResource, wbRequest);

		// Load content into an HTML page, and resolve load-time URLs:
		TextDocument page = new TextDocument(decodedResource, result,
				uriConverter);
		page.readFully(charSet);

		updatePage(page, httpRequest, httpResponse, context, decodedResource,
				results);

		// set the corrected length:
		int bytes = page.getBytes().length;
		headers.put(HttpHeaderOperation.HTTP_LENGTH_HEADER, String.valueOf(bytes));
		if (guessedCharsetHeader != null) {
			headers.put(guessedCharsetHeader, page.getCharSet());
		}

		// send back the headers:
		HttpHeaderOperation.sendHeaders(headers, httpResponse);

		// Tomcat will always send a charset... It's trying to be smarter than
		// we are. If the original page didn't include a "charset" as part of
		// the "Content-Type" HTTP header, then Tomcat will use the default..
		// who knows what that is, or what that will do to the page..
		// let's try explicitly setting it to what we used:
		httpResponse.setCharacterEncoding(page.getCharSet());

        if (overrideContentMimeType != null && !overrideContentMimeType.isEmpty()) {
            httpResponse.setContentType(overrideContentMimeType);
        }
        try {
        	page.writeToOutputStream(httpResponse.getOutputStream());
        } catch (IOException ex) {
        	// probably client has closed connection - swallow it
        	// so that it does not cause stack trace up somewhere.
        	LOGGER.info("error writing response: " + ex);
        }
	}

	/**
	 * @return the jspInserts
	 */
	public List<String> getJspInserts() {
		return jspInserts;
	}

	/**
	 * @param jspInserts the jspInserts to set
	 */
	public void setJspInserts(List<String> jspInserts) {
		this.jspInserts = jspInserts;
	}

	/**
	 * @return the charsetDetector
	 */
	public CharsetDetector getCharsetDetector() {
		return charsetDetector;
	}

	/**
	 * @param charsetDetector the charsetDetector to set
	 */
	public void setCharsetDetector(CharsetDetector charsetDetector) {
		this.charsetDetector = charsetDetector;
	}

	/**
	 * @return the String HTTP Header used to indicate what Wayback determined
	 * was the pages original charset 
	 */
	public String getGuessedCharsetHeader() {
		return guessedCharsetHeader;
	}

	/**
	 * @param guessedCharsetHeader the String HTTP Header value used to indicate
	 * to clients what Wayback determined was the pages original charset. If set
	 * to null, the header will be omitted.
	 */
	public void setGuessedCharsetHeader(String guessedCharsetHeader) {
		this.guessedCharsetHeader = guessedCharsetHeader;
	}

	public static Resource decodeResource(Resource resource) throws IOException {
		return decodeResource(resource, resource);
	}

    /**
     * @return the String HTTP Header 'Content-Type' used in overriding the archived
     * value during playback.
     */
    public String getOverrideContentMimeType() {
        return overrideContentMimeType;
    }

    /**
     * @param overrideContentMimeType the String HTTP Header 'Content-Type' used to
     * override the archived value.
     */
    public void setOverrideContentMimeType(String overrideContentMimeType) {
        this.overrideContentMimeType = overrideContentMimeType;
    }

	/**
	 * return gzip-decoding wrapper Resource if Resource has {@code Content-Encoding: gzip}.
	 * return {@code payloadResource} otherwise.
	 * <p>if headerResource's content is gzip-compressed (i.e. {@code Content-Encoding} is "{@code gzip}"),
	 * return a wrapping Resource that returns decoded content.</p>
	 * <p>As a side-effect, {@code Content-Encoding} and
	 * {@code Transfer-Encoding} headers are removed from {@code headersResource} (this happens only when
	 * {@code headerResoruce} is gzip-compressed.). It is assumed that {@code headerResource} and
	 * {@code payloadResource} are captures of identical response content.</p>
	 * <p>TODO: XArchiveHttpHeaderProcessor also does HTTP header removal. Check for refactoring case.</p>
	 * @param headersResource Resource to read HTTP headers from.
	 * @param payloadResource Resource to read content from (same as {@code headerResource} for regular captures,
	 * 	different Resource if headersResource is a revisit record.)
	 * @return
	 * @throws IOException
	 */
	public static Resource decodeResource(Resource headersResource,
			Resource payloadResource) throws IOException {
		// Content-Encoding may differ between headersResource and
		// payloadResource. We use Content-Encoding of payloadResource,
		// as we're reading content from it. Then update headersResource,
		// as we're rendering headers from headersResource.
		Map<String, String> headers = payloadResource.getHttpHeaders();

		if (headers != null) {
			String encoding = HttpHeaderOperation.getHeaderValue(headers,
					HttpHeaderOperation.HTTP_CONTENT_ENCODING);
			if (encoding != null) {
				final String lcEncoding = encoding.toLowerCase();
				Resource decodingResource = null;
				if (lcEncoding.equals(GzipDecodingResource.GZIP)) {
					decodingResource = new GzipDecodingResource(payloadResource);
				} else if (lcEncoding.equals(InflatingResource.CONTENT_ENCODING_NAME)) {
					decodingResource = new InflatingResource(payloadResource);
				}

				if (decodingResource != null) {
					// if headersResource (revisit) has Content-Encoding, set it aside.
					Map<String, String> revHeaders = headersResource.getHttpHeaders();
					String revEncoding = HttpHeaderOperation.getHeaderValue(
						revHeaders, HttpHeaderOperation.HTTP_CONTENT_ENCODING);
					if (revEncoding != null) {
						revHeaders.put(ORIG_ENCODING, encoding);
						HttpHeaderOperation.removeHeader(revHeaders,
							HttpHeaderOperation.HTTP_CONTENT_ENCODING);
					}

					if (HttpHeaderOperation.isChunkEncoded(revHeaders)) {
						HttpHeaderOperation.removeHeader(revHeaders,
								HttpHeaderOperation.HTTP_TRANSFER_ENC_HEADER);
					}

					return decodingResource;
				}
			}
		}

		return payloadResource;
	}

	/**
	 * return text to insert.
	 * <p>{@code jspInserts} are executed in sequence, concatenating
	 * their output.</p>
	 * @param page for calling {@code includeJspString} method
	 * @param httpRequest incoming request
	 * @param httpResponse outgoing response
	 * @param wbRequest wayback request info
	 * @param results captures
	 * @param result capture being replayed
	 * @param resource resource being replayed
	 * @return concatenated output of {@code jspInserts}
	 *  (empty if no {@code jspInserts} is configured.)
	 * @throws IOException error from {@link TextDocument#includeJspString}
	 * @throws ServletException error from {@link TextDocument#includeJspString}
	 */
	protected CharSequence buildInsertText(TextDocument page,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, CaptureSearchResults results,
			CaptureSearchResult result, Resource resource) throws IOException, ServletException {
		if (jspInserts == null || jspInserts.isEmpty())
			return "";
		StringBuilder toInsert = new StringBuilder(300);
		for (String jspName : jspInserts) {
			toInsert.append(page.includeJspString(jspName, httpRequest, httpResponse, wbRequest, results, result, resource));
		}
		return toInsert;
	}
}
