package org.archive.cdxserver.processor;

import java.util.HashMap;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class DupeCountProcessor extends WrappedProcessor {

    protected HashMap<String, DupeTrack> dupeHashmap = null;
    protected boolean showDupeCount = false;

    public final static String dupecount = "dupecount";

    class DupeTrack {
        int count = 0;
    }
    
    protected DupeTrack createDupeTrack()
    {
    	return new DupeTrack();
    }
    
    protected void handleLine(DupeTrack counter, CDXLine line, boolean isDupe) {
	    
    }

    public DupeCountProcessor(BaseProcessor output, boolean showDupeCount) {
        super(output);
        this.dupeHashmap = new HashMap<String, DupeTrack>();
        
        this.showDupeCount = showDupeCount;
    }

    @Override
    public int writeLine(CDXLine line) {
        String digest = line.getDigest();

        DupeTrack counter = dupeHashmap.get(digest);
        
        if (counter == null) {
            counter = createDupeTrack();
            dupeHashmap.put(digest, counter);
            if (showDupeCount) {
                line.setField(dupecount, "0");
            }
            handleLine(counter, line, false);
            
        } else {
            counter.count++;
            if (showDupeCount) {
                line.setField(dupecount, "" + counter.count);
            }
            handleLine(counter, line, true);
        }

        return inner.writeLine(line);
    }

	@Override
    public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
        if (showDupeCount) {
            format = super.modifyOutputFormat(format).addFieldNames(dupecount);
        }
        
        return format;
    }
}
