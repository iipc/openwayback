package org.archive.cdxserver.processor;

import java.util.LinkedList;
import java.util.List;

import org.archive.format.cdx.CDXLine;

public class ReverseRevisitResolver extends RevisitResolver {

	public ReverseRevisitResolver(BaseProcessor output, boolean showDupeCount) {
		super(output, showDupeCount);
	}
	
	static class ReverseRevisitTrack extends RevisitTrack {
		List<CDXLine> revisitLines;
		
		@Override
		final void revisit(CDXLine line) {
			if (revisitLines == null) {
				revisitLines = new LinkedList<CDXLine>();
			}
			revisitLines.add(line);
			fillBlankOrig(line);
		}
		
		@Override
		final void original(CDXLine line) {
			if (revisitLines != null) {
				for (CDXLine revisitLine : revisitLines) {
					fillRevisit(revisitLine, line);
				}
				revisitLines.clear();
			} else {
				fillBlankOrig(line);
			}
		}
	}
	
	@Override
	protected RevisitTrack createDupeTrack() {
		return new ReverseRevisitTrack();
	}
    
}
