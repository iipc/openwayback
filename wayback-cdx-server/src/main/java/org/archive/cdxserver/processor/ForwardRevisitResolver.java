package org.archive.cdxserver.processor;

import org.archive.format.cdx.CDXLine;

public class ForwardRevisitResolver extends RevisitResolver {

	public ForwardRevisitResolver(BaseProcessor output, boolean showDupeCount) {
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
	    
    	boolean currIsRevisit = isRevisit(line);
    	
    	if ((origLineDupeTrack.line == null) && !currIsRevisit) {
	    	origLineDupeTrack.line = line;
	    } else {	    	
	    	origLine = origLineDupeTrack.line;
	    }
        
        if ((origLine != null) && currIsRevisit) {
        	this.fillRevisit(line, origLine);
        } else {
        	this.fillBlankOrig(line);
        }
    }
    
}
