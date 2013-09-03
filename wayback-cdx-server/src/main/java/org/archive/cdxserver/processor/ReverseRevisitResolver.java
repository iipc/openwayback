package org.archive.cdxserver.processor;

import java.util.LinkedList;

import org.archive.format.cdx.CDXLine;

public class ReverseRevisitResolver extends RevisitResolver {

	public ReverseRevisitResolver(
			BaseProcessor output, 
			boolean showDupeCount) {
		
		super(output, showDupeCount);
	}
	
	class RevisitTrack extends DupeTrack {
		LinkedList<CDXLine> lines;
		
		void add(CDXLine line)
		{
			if (lines == null) {
				lines = new LinkedList<CDXLine>();
			}
			lines.add(line);
		}
		
		void fillRevisits(CDXLine origLine)
		{
			if (lines != null) {
				for (CDXLine revisitLine : lines) {
					ReverseRevisitResolver.this.fillRevisit(revisitLine, origLine);
				}
				lines.clear();
			}
		}
	}
	
	@Override
    protected DupeTrack createDupeTrack()
    {
    	return new RevisitTrack();
    }
    
	@Override
    protected void handleLine(DupeTrack counter, CDXLine line, boolean isDupe) {
    	RevisitTrack revisits = (RevisitTrack)counter;
		
		super.fillBlankOrig(line);
		
		if (isRevisit(line)) {
			revisits.add(line);
		} else {
			revisits.fillRevisits(line);
		}
    }
}
