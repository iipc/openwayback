package org.archive.cdxserver.processor;

import org.archive.format.cdx.CDXLine;

public class FowardRevisitResolver extends RevisitResolver {

	public FowardRevisitResolver(BaseProcessor output, boolean showDupeCount) {
	    super(output, showDupeCount);
    }
	
	class OrigLineDupeTrack extends DupeTrack
	{
		CDXLine line;
	}
	
    protected DupeTrack createDupeTrack()
    {
    	return new OrigLineDupeTrack();
    }
    
    protected void handleLine(DupeTrack counter, CDXLine line, boolean isDupe) {
	    OrigLineDupeTrack origLineDupeTrack = (OrigLineDupeTrack)counter;
    	
	    CDXLine origLine = null;
	    
    	if (!isDupe) {
	    	origLineDupeTrack.line = line;
	    } else {
	    	origLine = origLineDupeTrack.line;
	    }
        
        if ((origLine != null) && isRevisit(line)) {
        	this.fillRevisit(line, origLine);
        } else {
        	this.fillBlankOrig(origLine);
        }
    }
    
}
