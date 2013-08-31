package org.archive.cdxserver.processor;

import java.util.HashSet;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class SkipCountProcessor extends WrappedProcessor {
	
	protected final static String skipcount = "skipcount";
	protected final static String endtimestamp = "endtimestamp";
	protected final static String uniqcount = "uniqcount";
	
	protected CDXLine prevReadLine;
	protected CDXLine lastReadLine;
	
	protected CDXLine deferWriteLine;
	
	protected boolean lastLineSkipped = false;
	protected int skipCount = 0;
	
	protected boolean writeLastTimestamp;
	
	protected boolean writeUniqCount;
	protected HashSet<Integer> uniqDigestSet;

	public SkipCountProcessor(BaseProcessor base, boolean writeLastTimestamp)
	{
		super(base);
		this.writeLastTimestamp = writeLastTimestamp;
	}

	@Override
	public void trackLine(CDXLine cdxLine) {
		prevReadLine = lastReadLine;
		
		if (lastLineSkipped) {
			skipCount++;
			
			if (uniqDigestSet != null) {
				uniqDigestSet.add(cdxLine.getDigest().hashCode());
			}
		}
		
		lastReadLine = cdxLine;
		
		lastLineSkipped = true;
	}
	
	protected int writeDeferredLine()
	{
		int written = 0;
		
		if (deferWriteLine != null) {
			deferWriteLine.setField(skipcount, "" + skipCount);
			if (writeLastTimestamp) {
				deferWriteLine.setField(endtimestamp, (prevReadLine != null ? prevReadLine.getTimestamp() : "-"));
			}
			if (uniqDigestSet != null) {
				deferWriteLine.setField(uniqcount, "" + (uniqDigestSet.size() + 1));
			}
			written = inner.writeLine(deferWriteLine);
			deferWriteLine = null;
		}
		
		return written;
	}
	
	@Override
	public int writeLine(CDXLine cdxLine)
	{
		int written = writeDeferredLine();
		
		lastLineSkipped = false;
		skipCount = 0;
		
		if (uniqDigestSet != null) {
			uniqDigestSet.clear();
		}
		
		deferWriteLine = cdxLine;
		
		return written;
	}
	
	@Override
	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format)
	{
		format = super.modifyOutputFormat(format).addFieldNames(skipcount);
		
		if (writeLastTimestamp) {
			format = format.addFieldNames(endtimestamp);
		}
		
		if (uniqDigestSet != null) {
			format = format.addFieldNames(uniqcount);
		}
		
		return format;
	}

	@Override
	public void writeResumeKey(String resumeKey) {
		this.writeDeferredLine();
		super.writeResumeKey(resumeKey);
	}

	@Override
	public void end() {
		this.writeDeferredLine();
		super.end();
	}
}
