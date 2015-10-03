package org.archive.cdxserver.writer;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.archive.format.cdx.CDXLine;

public class PlainTextWriter extends HttpCDXWriter {
	
	public PlainTextWriter(HttpServletResponse response, boolean gzip) throws IOException
	{
		super(response, gzip);
		setContentType("text/plain; charset=\"UTF-8\"");
	}
	
	@Override
	public void begin() {

	}

	@Override
	public int writeLine(CDXLine line) {
		if (!includeBlockedCaptures && isBlocked(line))
			return 0;
		writer.println(line.toString());
		return 1;
	}

	@Override
	public void end() {

	}

	@Override
	public void writeResumeKey(String resumeKey) {
		writer.println();
		writer.println(resumeKey);
	}
}
