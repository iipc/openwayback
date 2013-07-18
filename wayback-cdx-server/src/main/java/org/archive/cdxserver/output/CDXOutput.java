package org.archive.cdxserver.output;

import java.io.PrintWriter;

import org.archive.format.cdx.CDXLine;

public interface CDXOutput {
	public void begin(PrintWriter writer);
	
	public int writeLine(PrintWriter writer, CDXLine line);
	public void writeResumeKey(PrintWriter writer, String resumeKey);
	
	public void end(PrintWriter writer);
}
