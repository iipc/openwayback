package org.archive.wayback.webapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class PerfWritingHttpServletResponse extends HttpServletResponseWrapper {
	
	protected Enum<?> perfStat;
	protected String perfStatsHeader;
	protected boolean hasWritten;
	protected HttpServletResponse httpResponse;
	
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
		
		PerfStats.timeEnd(perfStat);
		
		if (perfStatsHeader != null) {
			httpResponse.setHeader(perfStatsHeader, PerfStats.getAllStats());
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
}
