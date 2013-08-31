package org.archive.cdxserver.processor;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public interface BaseProcessor {
	public void begin();
	
	public void trackLine(CDXLine line);
	
	public int writeLine(CDXLine line);
	public void writeResumeKey(String resumeKey);
	
	public void end();

	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format);
}
