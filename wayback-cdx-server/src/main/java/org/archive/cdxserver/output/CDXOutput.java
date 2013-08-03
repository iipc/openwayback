package org.archive.cdxserver.output;

import java.io.PrintWriter;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public interface CDXOutput {
	public void begin(PrintWriter writer);
	
	public void trackLine(CDXLine line);
	
	public boolean writeLine(PrintWriter writer, CDXLine line);
	public void writeResumeKey(PrintWriter writer, String resumeKey);
	
	public void end(PrintWriter writer);

	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format);
}
