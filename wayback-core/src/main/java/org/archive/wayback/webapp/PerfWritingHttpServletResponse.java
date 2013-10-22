package org.archive.wayback.webapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class PerfWritingHttpServletResponse extends HttpServletResponseWrapper {
	
	protected Enum<?> perfStat;
	protected String perfStatsHeader;
	protected boolean hasWritten;
	protected HttpServletResponse httpResponse;
	
	protected int expireTimeout = 60;
	
	protected String requestURI;
	
	public PerfWritingHttpServletResponse(HttpServletResponse response, Enum<?> stat, String perfStatsHeader)
	{
		super(response);
		
		this.httpResponse = response;		
		this.perfStat = stat;
		this.perfStatsHeader = perfStatsHeader;
	}
	
	public void writePerfStats()
	{
		if (hasWritten) {
			return;
		}
		
		long elapsed = PerfStats.timeEnd(perfStat);
		
		if (perfStatsHeader != null) {
			httpResponse.setHeader(perfStatsHeader, PerfStats.getAllStats());
		}
		
		if (requestURI != null) {
			Cookie cookie = new Cookie("wb_total_perf", String.valueOf(elapsed));
			cookie.setMaxAge(expireTimeout);
			//cookie.setDomain(domainName);
			cookie.setPath(requestURI);
			httpResponse.addCookie(cookie);
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

	public void enablePerfCookie(String requestURI) {
		this.requestURI = requestURI;
    }
}
