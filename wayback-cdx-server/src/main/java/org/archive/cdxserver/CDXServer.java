
package org.archive.cdxserver;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.output.CDXDefaultTextOutput;
import org.archive.cdxserver.output.CDXDupeCountWriter;
import org.archive.cdxserver.output.CDXJsonOutput;
import org.archive.cdxserver.output.CDXOutput;
import org.archive.cdxserver.output.CDXSkipCountWriter;
import org.archive.cdxserver.output.LastNLineOutput;
import org.archive.format.cdx.CDXInputSource;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.CDXLineFactory;
import org.archive.format.cdx.FieldSplitFormat;
import org.archive.format.cdx.StandardCDXLineFactory;
import org.archive.format.gzip.zipnum.ZipNumCluster;
import org.archive.format.gzip.zipnum.ZipNumIndex.PageResult;
import org.archive.format.gzip.zipnum.ZipNumParams;
import org.archive.url.UrlSurtRangeComputer.MatchType;
import org.archive.util.PrefixFieldCollapser;
import org.archive.util.RegexFieldMatcher;
import org.archive.util.iterator.CloseableIterator;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class CDXServer extends BaseCDXServer {

	public final static String X_NUM_PAGES = "X-CDX-Num-Pages";
	public final static String X_MAX_LINES = "X-CDX-Max-Lines";

	protected ZipNumCluster zipnumSource;
	protected CDXInputSource cdxSource;
	protected ZipNumParams zipParams;
	protected ZipNumParams dedupedParams;
	
	protected String cdxFormat = null;
	
	protected FieldSplitFormat defaultCdxFormat;
	protected FieldSplitFormat publicCdxFields;

	@Override
	public void afterPropertiesSet() throws Exception {
		zipParams = new ZipNumParams();
		zipParams.setMaxAggregateBlocks(pageSize);
		zipParams.setMaxBlocks(pageSize);
		zipParams.setTimestampDedupLength(0);

		dedupedParams = new ZipNumParams();
		dedupedParams.setMaxAggregateBlocks(pageSize);
		dedupedParams.setMaxBlocks(pageSize);
		dedupedParams.setTimestampDedupLength(8);

		if (cdxSource == null) {
			cdxSource = zipnumSource;
		}
		
		CDXLineFactory cdxLineFactory = new StandardCDXLineFactory(cdxFormat);
		defaultCdxFormat = cdxLineFactory.getParseFormat();
		
		if (authChecker != null && authChecker.getPublicCdxFields() != null) {
			publicCdxFields = new FieldSplitFormat(authChecker.getPublicCdxFields());
		}

		super.afterPropertiesSet();
	}

	protected int pageSize = 1;
	protected int queryMaxLimit = 1;

	public ZipNumCluster getZipnumSource() {
		return zipnumSource;
	}

	public void setZipnumSource(ZipNumCluster zipnumSource) {
		this.zipnumSource = zipnumSource;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getCdxFormat() {
		return cdxFormat;
	}

	public void setCdxFormat(String cdxFormat) {
		this.cdxFormat = cdxFormat;
	}

	public int getQueryMaxLimit() {
		return queryMaxLimit;
	}

	public void setQueryMaxLimit(int queryMaxLimit) {
		this.queryMaxLimit = queryMaxLimit;
	}

	public CDXInputSource getCdxSource() {
		return cdxSource;
	}

	public void setCdxSource(CDXInputSource cdxSource) {
		this.cdxSource = cdxSource;
	}

	public void getCdx(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletRequestBindingException {
		String url = ServletRequestUtils.getRequiredStringParameter(request, "url");
		MatchType matchType = null;

		String matchTypeStr = ServletRequestUtils.getStringParameter(request, "matchType", null);
		if (matchTypeStr != null) {
			matchType = MatchType.valueOf(matchTypeStr);
		}

		String from = ServletRequestUtils.getStringParameter(request, "from", "");
		String to = ServletRequestUtils.getStringParameter(request, "to", "");

		boolean gzip = ServletRequestUtils.getBooleanParameter(request, "gzip", true);
		String output = ServletRequestUtils.getStringParameter(request, "output", "");

		String[] filter = ServletRequestUtils.getStringParameters(request, "filter");
		String[] collapse = ServletRequestUtils.getStringParameters(request, "collapse");
		
		boolean dupeCount = ServletRequestUtils.getBooleanParameter(request, "showDupeCount");
		boolean skipCount = ServletRequestUtils.getBooleanParameter(request, "showSkipCount");
		boolean lastSkipTimestamp = ServletRequestUtils.getBooleanParameter(request, "lastSkipTimestamp");
		
		int offset = ServletRequestUtils.getIntParameter(request, "offset", 0);
		int limit = ServletRequestUtils.getIntParameter(request, "limit", 0);
		boolean fastLatest = ServletRequestUtils.getBooleanParameter(request, "fastLatest", false);
		String fl = ServletRequestUtils.getStringParameter(request, "fl", "");
		
		int page = ServletRequestUtils.getIntParameter(request, "page", -1);

		boolean showNumPages = ServletRequestUtils.getBooleanParameter(request, "showNumPages", false);
		boolean showPagedIndex = ServletRequestUtils.getBooleanParameter(request, "showPagedIndex", false);

		String resumeKey = ServletRequestUtils.getStringParameter(request, "resumeKey", "");
		boolean showResumeKey = ServletRequestUtils.getBooleanParameter(request, "showResumeKey", false);

		this.getCdx(request, response, url, matchType, from, to, gzip, output, filter, collapse, dupeCount, skipCount, lastSkipTimestamp,
				offset, limit, fastLatest, fl, page, showNumPages, showPagedIndex,
				resumeKey, showResumeKey);
	}

	@SuppressWarnings("resource")
	@RequestMapping(value = { "/cdx" })
	public void getCdx(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "url") String url,
			@RequestParam(value = "matchType", required = false) MatchType matchType,

			@RequestParam(value = "from", defaultValue = "") String from,
			@RequestParam(value = "to", defaultValue = "") String to,

			@RequestParam(value = "gzip", defaultValue = "true") boolean gzip, 
			@RequestParam(value = "output", defaultValue = "") String output,

			@RequestParam(value = "filter", required = false) String[] filter, 
			@RequestParam(value = "collapse", required = false) String[] collapse,
			
			@RequestParam(value = "showDupeCount", defaultValue = "false") boolean showDupeCount,
			@RequestParam(value = "showSkipCount", defaultValue = "false") boolean showSkipCount,
			@RequestParam(value = "lastSkipTimestamp", defaultValue = "false") boolean lastSkipTimestamp,
			
			@RequestParam(value = "offset", defaultValue = "0") int offset, 
			@RequestParam(value = "limit", defaultValue = "0") int limit,
			@RequestParam(value = "fastLatest", defaultValue = "false") boolean fastLatest,
			@RequestParam(value = "fl", defaultValue = "") String fl,

			@RequestParam(value = "page", defaultValue = "-1") int page, 
			@RequestParam(value = "showNumPages", defaultValue = "false") boolean showNumPages,
			@RequestParam(value = "showPagedIndex", defaultValue = "false") boolean showPagedIndex,

			@RequestParam(value = "resumeKey", defaultValue = "") String resumeKey,
			@RequestParam(value = "showResumeKey", defaultValue = "false") boolean showResumeKey) throws IOException {
		CloseableIterator<String> iter = null;
		PrintWriter writer = null;

		AuthToken authToken = super.initAuthToken(request);

		try {
			prepareResponse(response);

			if (!authToken.isAllUrlAccessAllowed() && !authChecker.checkAccess(url)) {
				if (showNumPages) {
					// Default to 1 page even if no results
					response.setHeader(X_NUM_PAGES, "1");
					response.getWriter().println("1");
				}
				return;
			}

			// Check for wildcards as shortcuts for matchType
			if (matchType == null) {
				if (url.startsWith("*.")) {
					matchType = MatchType.domain;
					url = url.substring(2);
				} else if (url.endsWith("*")) {
					matchType = MatchType.prefix;
					url = url.substring(0, url.length() - 1);
				} else {
					matchType = MatchType.exact;
				}
			}

			String startEndUrl[] = urlSurtRangeComputer.determineRange(url, matchType, "", "");

			if (startEndUrl == null) {
				response.setStatus(400);
				response.getWriter().println("Sorry, matchType=" + matchType.name() + " is not supported by this server");
				return;
			}

			int maxLimit;

			// Optimize: always fastLatest if just last line
			fastLatest = fastLatest || (limit == -1);

			// Paged query
			if (page >= 0 || showNumPages) {
				if (zipnumSource == null) {
					response.setStatus(400);
					response.getWriter().println("Sorry, this server is not configured to support paged query. Remove page= param and try again.");
					return;
				}

				PageResult pageResult = zipnumSource.getNthPage(startEndUrl, page, pageSize, showNumPages);

				response.setHeader(X_NUM_PAGES, "" + pageResult.numPages);

				if (showNumPages) {
					response.getWriter().println(pageResult.numPages);
					return;
				}
				
				iter = pageResult.iter;

				if (iter == null) {
					return;
				}

				if (showPagedIndex && authToken.isAllUrlAccessAllowed()) {
					response.addHeader(X_MAX_LINES, "" + pageSize);
					writeIdxResponse(response.getWriter(), iter);
					return;
				}

				if (!resumeKey.isEmpty()) {
					startEndUrl[0] = URLDecoder.decode(resumeKey, "UTF-8");
				} else if (!from.isEmpty()) {
					startEndUrl[0] += " " + from;
				} else if (fastLatest) {
					startEndUrl[0] += "!";
				}

				iter = zipnumSource.getCDXIterator(iter, startEndUrl[0], startEndUrl[1], page, pageResult.numPages, zipParams);

				response.addHeader(X_MAX_LINES, "" + (zipnumSource.getCdxLinesPerBlock() * pageSize));

				// Page size determines the max limit here
				maxLimit = Integer.MAX_VALUE;

			} else {
				// Non-Paged Merged query
				String searchKey = null;

				if (!resumeKey.isEmpty()) {
					searchKey = URLDecoder.decode(resumeKey, "UTF-8");
					startEndUrl[0] = resumeKey;
				} else if (!from.isEmpty()) {
					searchKey = startEndUrl[0] + " " + from;
				} else if (fastLatest) {
					searchKey = startEndUrl[0] + "!";
				} else {
					searchKey = startEndUrl[0];
				}

				iter = cdxSource.getCDXIterator(searchKey, startEndUrl[0], startEndUrl[1], dedupedParams);

				maxLimit = this.queryMaxLimit;
			}

			if (gzip) {
				writer = getGzipWriter(response);
			} else {
				writer = response.getWriter();
			}

			CDXOutput outputProcessor = null;

			if (output.equals("json")) {
				outputProcessor = new CDXJsonOutput();
				response.setContentType("application/json");
			} else {
				outputProcessor = new CDXDefaultTextOutput();
			}

			if (limit < 0) {
				limit = Math.min(-limit, maxLimit);
				outputProcessor = new LastNLineOutput(outputProcessor, limit);
			} else if (limit == 0) {
				limit = maxLimit;
			} else {
				limit = Math.min(limit, maxLimit);
			}
			
			if (showDupeCount) {
				outputProcessor = new CDXDupeCountWriter(outputProcessor);
			}
			
			if (showSkipCount) {
				outputProcessor = new CDXSkipCountWriter(outputProcessor, lastSkipTimestamp);
			}

			writeCdxResponse(outputProcessor, writer, iter, from, to, filter, collapse, fl, offset, limit, maxLimit, showResumeKey, authToken, matchType);

			writer.flush();

		} catch (URIException e) {
			response.setStatus(400);
			response.getWriter().println(e.toString());
			response.getWriter().flush();
		} catch (URISyntaxException e) {
			response.setStatus(400);
			response.getWriter().println(e.toString());
			response.getWriter().flush();
		} finally {
			if (iter != null) {
				iter.close();
			}

			if (writer != null) {
				writer.close();
			}
		}
	}

	// TODO: Support idx/summary in json?
	protected void writeIdxResponse(PrintWriter writer, CloseableIterator<String> iter) {
		while (iter.hasNext()) {
			writer.println(iter.next());
		}
	}

	protected void writeCdxResponse(
			CDXOutput outputProcessor, 
			PrintWriter writer, 
			CloseableIterator<String> cdx, 
			
			String from, 
			String to,
			
			String[] filter,
			String[] collapse,
			
			String outputFL,
			
			int offset,
			int writeLimit,
			int readLimit,
			boolean showResumeKey,
			
			AuthToken authToken, 
			MatchType matchType) {
		
		FieldSplitFormat parseFormat = outputProcessor.modifyOutputFormat(defaultCdxFormat);

		RegexFieldMatcher filterMatcher = null;

		if (filter != null) {
			filterMatcher = new RegexFieldMatcher(filter, parseFormat);
		}

		PrefixFieldCollapser collapser = null;

		if (collapse != null) {
			collapser = new PrefixFieldCollapser(collapse, parseFormat);
		}

		CDXLine prev = null;
		CDXLine line = null;

		boolean prevUrlAllowed = true;
		
		FieldSplitFormat outputFields = null;
		
		if (!authToken.isAllCdxFieldsAllowed()) {
			outputFields = this.publicCdxFields;
		}
		
		if (!outputFL.isEmpty()) {
			if (outputFields == null) {
				outputFields = parseFormat;
			}
			try {
				outputFields = outputFields.createSubset(URLDecoder.decode(outputFL, "UTF-8"));
			} catch (UnsupportedEncodingException e) {

			}
		} else if (outputFields != null) {
			outputFields = parseFormat.createSubset(outputFields);
		}

		outputProcessor.begin(writer);

		int writeCount = 0;
		long allCount = 0;

		while (cdx.hasNext() && ((writeLimit == 0) || (writeCount < writeLimit)) && (allCount < readLimit)) {
			if ((allCount % 1000) == 0) {
				if (writer.checkError()) {
					break;
				} else {
					writer.flush();
				}
			}
			
			String rawLine = cdx.next();
			allCount++;

			if (offset > 0) {
				--offset;
				continue;
			}

			prev = line;
			
			line = new CDXLine(rawLine, parseFormat);

			if (!authToken.isAllUrlAccessAllowed()) {
				if ((matchType != MatchType.exact) && ((prev == null) || !line.getUrlKey().equals(prev.getUrlKey()))) {
					prevUrlAllowed = authChecker.checkAccess(line.getOriginalUrl());
				}

				if (!prevUrlAllowed) {
					continue;
				}
			}
			
			outputProcessor.trackLine(line);

			// Timestamp Range Filtering
			String timestamp = line.getTimestamp();

			if (!from.isEmpty() && (timestamp.compareTo(from) < 0)) {
				continue;
			}

			if (!to.isEmpty() && (timestamp.compareTo(to) > 0) && !timestamp.startsWith(to)) {
				if (matchType == MatchType.exact) {
					break;
				} else {
					continue;
				}
			}

			// Check regex matcher if it exists
			if ((filterMatcher != null) && !filterMatcher.matches(line)) {
				continue;
			}

			// Check collapser
			if ((collapser != null) && !collapser.isUnique(line)) {
				continue;
			}

			// Filter to only include output fields
			if (outputFields != null) {
				line = new CDXLine(line, outputFields);
			}
			
			if (outputProcessor.writeLine(writer, line)) {
				writeCount++;
			}

			if (Thread.interrupted()) {
				break;
			}
		}

		if (showResumeKey && (line != null) && (writeLimit > 0) && (writeCount >= writeLimit)) {
			StringBuilder sb = new StringBuilder();
			sb.append(line.getUrlKey());
			sb.append(line.getTimestamp());
			sb.append(' ');
			sb.append('!');
			String resumeKey;
			try {
				resumeKey = URLEncoder.encode(sb.toString(), "UTF-8");
				outputProcessor.writeResumeKey(writer, resumeKey);
			} catch (UnsupportedEncodingException e) {

			}
		}

		outputProcessor.end(writer);
	}
	
}