package org.archive.cdxserver.output;

import java.io.PrintWriter;
import java.util.HashMap;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class CDXDupeCountWriter extends WrappedCDXOutput {

    protected HashMap<String, DupeTrack> dupeHashmap = null;
    protected boolean resolveRevisits = false;
    protected boolean showDupeCount = false;

    public final static String dupecount = "dupecount";

    public final static String origfilename = "orig.filename";
    public final static String origoffset = "orig.offset";
    public final static String origlength = "orig.length";

    class DupeTrack {
        CDXLine line;
        int count;

        DupeTrack(CDXLine line) {
            this.line = line;
            count = 0;
        }
    }

    public CDXDupeCountWriter(CDXOutput output, boolean showDupeCount, boolean resolveRevisits) {
        super(output);
        this.dupeHashmap = new HashMap<String, DupeTrack>();
        this.showDupeCount = showDupeCount;
        this.resolveRevisits = resolveRevisits;
    }

    @Override
    public int writeLine(PrintWriter writer, CDXLine line) {
        String digest = line.getDigest();

        DupeTrack counter = dupeHashmap.get(digest);
        CDXLine origLine = null;
        
        if (counter == null) {
            counter = new DupeTrack(line);
            dupeHashmap.put(digest, counter);
            if (showDupeCount) {
                line.setField(dupecount, "0");
            }
        } else {
            counter.count++;
            origLine = counter.line;
            if (showDupeCount) {
                line.setField(dupecount, "" + counter.count);
            }
        }
        
        if (resolveRevisits) {
            boolean isRevisit = (line.getMimeType().equals("warc/revisit"));
        
            if (isRevisit && (origLine != null)) {           
                line.setMimeType(origLine.getMimeType());
                
                line.setField(origlength, origLine.getLength());
                line.setField(origoffset, origLine.getOffset());
                line.setField(origfilename, origLine.getFilename());
            } else {
                line.setField(origlength, CDXLine.EMPTY_VALUE);
                line.setField(origoffset, CDXLine.EMPTY_VALUE);
                line.setField(origfilename, CDXLine.EMPTY_VALUE);            
            }
        }

        return inner.writeLine(writer, line);
    }

    @Override
    public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
        if (resolveRevisits) {
            format = super.modifyOutputFormat(format).addFieldNames(origlength,
                    origoffset, origfilename);
        }
        if (showDupeCount) {
            format = super.modifyOutputFormat(format).addFieldNames(dupecount);
        }
        return format;
    }
}
