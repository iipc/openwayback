package org.archive.cdxserver.processor;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class WrappedProcessor implements BaseProcessor {
	
	protected BaseProcessor inner;
	
	public WrappedProcessor(BaseProcessor output)
	{
		this.inner = output;
	}

	@Override
	public void begin() {
		inner.begin();
	}

	@Override
	public int writeLine(CDXLine line) {
		return inner.writeLine(line);
	}

	@Override
	public void writeResumeKey(String resumeKey) {
		inner.writeResumeKey(resumeKey);
	}

	@Override
	public void end() {
		inner.end();
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
