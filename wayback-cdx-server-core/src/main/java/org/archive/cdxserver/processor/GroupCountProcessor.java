package org.archive.cdxserver.processor;

import java.util.HashSet;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class GroupCountProcessor extends WrappedProcessor {
	
	public final static String groupcount = "groupcount";
	public final static String endtimestamp = "endtimestamp";
	public final static String uniqcount = "uniqcount";
	
	protected CDXLine prevReadLine;
	protected CDXLine lastReadLine;
	
	protected CDXLine deferWriteLine;
	
	protected boolean lastLineSkipped = false;
	protected int skipCount = 0;
	
	protected boolean writeLastTimestamp;
	
	protected HashSet<Integer> uniqDigestSet;

	public GroupCountProcessor(BaseProcessor base, boolean writeLastTimestamp, boolean writeUniqCount)
	{
		super(base);
		this.writeLastTimestamp = writeLastTimestamp;
		if (writeUniqCount) {
			uniqDigestSet = new HashSet<Integer>();
		}
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
			deferWriteLine.setField(groupcount, "" + (skipCount + 1));
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
		format = super.modifyOutputFormat(format).addFieldNames(groupcount);
		
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
