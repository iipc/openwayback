package org.archive.cdxserver.processor;

import org.archive.cdxserver.format.CDXFormat;
import org.archive.format.cdx.CDXLine;

/**
 * Base class for intermediary processors.
 * 
 * Note: {@code inner} processor is downstream (closer to the final output processor).
 */
public class WrappedProcessor implements BaseProcessor {
	
	protected BaseProcessor inner;
	
	public WrappedProcessor(BaseProcessor output) {
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
	public CDXFormat modifyOutputFormat(CDXFormat format) {
		String[] moreFields = extraFields();
		if (moreFields != null && moreFields.length > 0) {
			format = format.extend(moreFields);
		}
		return inner.modifyOutputFormat(format);
	}
	
	/**
	 * Return an array of field names to add to the format for
	 * {@link WaybackCDXLineFactory}.
	 * <p>
	 * Return {@code null} if processor does not need additional fields
	 * (base implementation).
	 * </p>
	 * <p>
	 * If intermediary processor ever removes fields, it should override
	 * {@link #modifyOutputFormat(CDXFormat)}.
	 * </p>
	 * @return
	 */
	protected String[] extraFields() {
		return null;
	}
}
