package org.archive.cdxserver.output;

import java.io.PrintWriter;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class CDXSkipCountWriter extends WrappedCDXOutput {
	
	protected final static String skipcount = "skipcount";
	protected final static String endtimestamp = "endtimestamp";
	
	protected CDXLine prevReadLine;
	protected CDXLine lastReadLine;
	
	protected CDXLine deferWriteLine;
	
	protected boolean lastLineSkipped = false;
	protected int skipCount = 0;
	
	protected boolean writeLastTimestamp;

	public CDXSkipCountWriter(CDXOutput base, boolean writeLastTimestamp)
	{
		super(base);
		this.writeLastTimestamp = writeLastTimestamp;
	}

	@Override
	public void trackLine(CDXLine cdxLine) {
		prevReadLine = lastReadLine;
		
		if (lastLineSkipped) {
			skipCount++;
		}
		
		lastReadLine = cdxLine;
		
		lastLineSkipped = true;
	}
	
	protected boolean writeDeferredLine(PrintWriter writer)
	{
		boolean result = false;
		
		if (deferWriteLine != null) {
			deferWriteLine.setField(skipcount, "" + skipCount);
			if (writeLastTimestamp) {
				deferWriteLine.setField(endtimestamp, (prevReadLine != null ? prevReadLine.getTimestamp() : "-"));
			}
			result = inner.writeLine(writer, deferWriteLine);
			deferWriteLine = null;
		}
		
		return result;
	}
	
	@Override
	public boolean writeLine(PrintWriter writer, CDXLine cdxLine)
	{
		boolean written = writeDeferredLine(writer);
		
		lastLineSkipped = false;
		skipCount = 0;
		
		deferWriteLine = cdxLine;
		
		return written;
	}
	
	@Override
	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
		
		//TODO: generalize to support more fields?
		if (writeLastTimestamp) {
			return super.modifyOutputFormat(format).addFieldNames(skipcount, endtimestamp);
		} else {
			return super.modifyOutputFormat(format).addFieldNames(skipcount);
		}
	}

	@Override
	public void writeResumeKey(PrintWriter writer, String resumeKey) {
		this.writeDeferredLine(writer);
		super.writeResumeKey(writer, resumeKey);
	}

	@Override
	public void end(PrintWriter writer) {
		this.writeDeferredLine(writer);
		super.end(writer);
	}
}
