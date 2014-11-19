package org.archive.wayback.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.archive.wayback.webapp.PerfStats.OutputFormat;
import org.archive.wayback.webapp.PerfStats.PerfStatEntry;

public class PerfWritingHttpServletResponse extends HttpServletResponseWrapper {

	protected final Enum<?> perfStat;
	protected final String perfStatsHeader;
	protected boolean hasWritten;
	protected final HttpServletResponse httpResponse;
	protected OutputFormat outputFormat;

	protected int expireTimeout = 60;

	protected final String requestURI;
	protected boolean perfCookie = false;

	/**
	 * Initialize with default output format.
	 * @param request
	 * @param response
	 * @param stat
	 * @param perfStatsHeader
	 */
	public PerfWritingHttpServletResponse(HttpServletRequest request,
			HttpServletResponse response, Enum<?> stat, String perfStatsHeader) {
		this(request, response, stat, perfStatsHeader, OutputFormat.BRACKET);
	}

	/**
	 * Initialize with all parameters.
	 * @param request {@code requestURI} is used as cookie path
	 * @param response wrapped response
	 * @param stat names stat for <i>total</i> elapsed time.
	 * @param perfStatsHeader names HTTP header field for dumping all stats.
	 * @param format format of {@code perfStatsHeader}
	 */
	public PerfWritingHttpServletResponse(HttpServletRequest request,
			HttpServletResponse response, Enum<?> stat, String perfStatsHeader,
			OutputFormat format) {
		super(response);
		this.httpResponse = response;
		this.requestURI = request.getRequestURI();
		this.perfStat = stat;
		this.perfStatsHeader = perfStatsHeader;
		this.outputFormat = format;
	}

	/**
	 * Write performance metrics to HTTP header field and Cookie.
	 * You don't need to call this method explicitly. It is called
	 * implicitly by calls to {@link #sendError(int)}, {@link #sendRedirect(String)},
	 * {@link #getWriter()} or {@link #getOutputStream()}.
	 * 2014-11-17 Now it doesn't call {@code timeEnd} for
	 * {@code perfStat}. Be sure to call {@code endNow()} explicitly.
	 */
	public void writePerfStats() {
		if (hasWritten) {
			return;
		}

		// call timeEnd only if it's not already called, so as
		// not to change its value. 
		long elapsed = PerfStats.getTotal(perfStat);
		if (elapsed <= 0) {
			elapsed = PerfStats.timeEnd(perfStat);
		}

		if (perfStatsHeader != null) {
			httpResponse.setHeader(perfStatsHeader, PerfStats.getAllStats(outputFormat));
		}

		if (perfCookie && requestURI != null) {
			Cookie cookie = new Cookie("wb_total_perf", String.valueOf(elapsed));
			cookie.setMaxAge(expireTimeout);
			//cookie.setDomain(domainName);
			cookie.setPath(requestURI);
			try {
				httpResponse.addCookie(cookie);
			} catch (IllegalArgumentException ex) {
				Logger logger = Logger.getLogger(getClass().getName());
				logger.warning("addCookie failed for " + cookie + " (path=\"" +
						requestURI + "\"): " + ex.getMessage());
			}
		}

		hasWritten = true;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		writePerfStats();
		super.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		writePerfStats();
		super.sendError(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		writePerfStats();
		super.sendRedirect(location);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		writePerfStats();
		return super.getOutputStream();
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		writePerfStats();
		return super.getWriter();
	}

	/**
	 * @deprecated 1.8.1, no replacement. this method has no effect.
	 */
	public void enablePerfCookie() {
		this.perfCookie = true;
	}

	/**
	 * Set to {@code true} if {@code wb_total_perf} cookie should be set
	 * in the response. In general this is a bad idea because it'll defeat
	 * front-end caching. As such, this is off by default.
	 * @param perfCookie {@code true} for sending out the cookie
	 */
	public void setPerfCookie(boolean perfCookie) {
		this.perfCookie = perfCookie;
	}
}
