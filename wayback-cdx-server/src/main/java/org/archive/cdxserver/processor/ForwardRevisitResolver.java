package org.archive.cdxserver.processor;

import org.archive.format.cdx.CDXLine;

public class ForwardRevisitResolver extends RevisitResolver {

	public ForwardRevisitResolver(BaseProcessor output, boolean showDupeCount) {
	    super(output, showDupeCount);
    }
	
	static class ForwardRevisitTrack extends RevisitTrack {
		CDXLine origLine;

		@Override
		final void revisit(CDXLine line) {
			if (origLine != null) {
				fillRevisit(line, origLine);
			} else {
				fillBlankOrig(line);
			}
		}

		@Override
		final void original(CDXLine line) {
			if (origLine == null) {
				origLine = line;
			}
			fillBlankOrig(line);
		}
	}
	
	@Override
	protected RevisitTrack createDupeTrack() {
		return new ForwardRevisitTrack();
	}
    
}
