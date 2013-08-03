package org.archive.cdxserver.output;

import java.io.PrintWriter;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class WrappedCDXOutput implements CDXOutput {
	
	protected CDXOutput inner;
	
	public WrappedCDXOutput(CDXOutput output)
	{
		this.inner = output;
	}

	@Override
	public void begin(PrintWriter writer) {
		inner.begin(writer);
	}

	@Override
	public boolean writeLine(PrintWriter writer, CDXLine line) {
		return inner.writeLine(writer, line);
	}

	@Override
	public void writeResumeKey(PrintWriter writer, String resumeKey) {
		inner.writeResumeKey(writer, resumeKey);
	}

	@Override
	public void end(PrintWriter writer) {
		inner.end(writer);
	}

	@Override
	public void trackLine(CDXLine line) {
		inner.trackLine(line);
	}
	
	@Override
	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
		return inner.modifyOutputFormat(format);
	}
}
