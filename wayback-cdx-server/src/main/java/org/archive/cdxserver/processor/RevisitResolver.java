package org.archive.cdxserver.processor;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public abstract class RevisitResolver extends DupeCountProcessor {

	public final static String origfilename = "orig.filename";
	public final static String origoffset = "orig.offset";
	public final static String origlength = "orig.length";

	public RevisitResolver(BaseProcessor output, boolean showDupeCount) {
		super(output, showDupeCount);
	}
	
	protected static void fillBlankOrig(CDXLine line) {
		line.setField(origlength, CDXLine.EMPTY_VALUE);
		line.setField(origoffset, CDXLine.EMPTY_VALUE);
		line.setField(origfilename, CDXLine.EMPTY_VALUE);
	}

	protected static void fillRevisit(CDXLine line, CDXLine origLine) {
		line.setMimeType(origLine.getMimeType());
		line.setStatusCode(origLine.getStatusCode());

		line.setField(origlength, origLine.getLength());
		line.setField(origoffset, origLine.getOffset());
		line.setField(origfilename, origLine.getFilename());
	}
    
	protected static boolean isRevisit(CDXLine line) {
		return (line.getMimeType().equals("warc/revisit") || line.getFilename()
			.equals(CDXLine.EMPTY_VALUE));
	}
    
	@Override
	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
		format = super.modifyOutputFormat(format).addFieldNames(origlength, origoffset, origfilename);
		return format;
	}

	static abstract class RevisitTrack extends DupeTrack {
		abstract void revisit(CDXLine line);
		abstract void original(CDXLine line);
	}

	@Override
    protected void handleLine(DupeTrack track, CDXLine line) {
		RevisitTrack revisitTrack = (RevisitTrack)track;
		if (isRevisit(line)) {
			revisitTrack.revisit(line);
		} else {
			revisitTrack.original(line);
		}
	}
}
