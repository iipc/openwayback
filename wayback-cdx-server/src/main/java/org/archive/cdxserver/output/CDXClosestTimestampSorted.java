package org.archive.cdxserver.output;

import java.io.PrintWriter;
import java.util.Date;
import java.util.TreeMap;

import org.archive.format.cdx.CDXLine;
import org.archive.util.ArchiveUtils;

public class CDXClosestTimestampSorted extends WrappedCDXOutput {
    
    enum Dir
    {
        ANY,
        PREV,
        NEXT,
    };
    
    Dir dir;
    TreeMap<Long, CDXLine> closestLines;
    Long target;
    int limit;
    boolean done = false;
    
    protected Long convTimestamp(String timestamp)
    {        
        return ArchiveUtils.getDate(timestamp, new Date()).getTime();
    }
    
    public CDXClosestTimestampSorted(CDXOutput output, String target, int limit) {
        super(output);
        
        if (target.startsWith("-")) {
            target = target.substring(1);
            dir = Dir.PREV;
        } else if (target.startsWith("^")) {
            target = target.substring(1);
            dir = Dir.NEXT;
        } else {
            dir = Dir.ANY;
        }
        
        this.target = convTimestamp(target);
        this.limit = (limit > 0 ? limit : Integer.MAX_VALUE);
        closestLines = new TreeMap<Long, CDXLine>();
    }

    @Override
    public int writeLine(PrintWriter writer, CDXLine line) {
        if (done) {
            return Integer.MAX_VALUE;
        }
        
        Long curr = convTimestamp(line.getTimestamp());
        Long diff;
        
        switch (dir) {            
        case PREV:
            if (curr < target) {
                diff = target - curr;
            } else {
                diff = target + curr;
            }
            break;
            
        case NEXT:
            if (curr > target) {
                diff = curr - target;
            } else {
                diff = (target - curr) + target * 2;
            }
            break;
            
        case ANY:
        default:
            diff = Math.abs(curr - target);
            break;
        }
        
        if (closestLines.size() == limit) {
            // Assumes ascending timestamp input 
            if (diff > closestLines.lastKey()) {
                done = true;
                return writeSorted(writer);
            }
        }
        
        closestLines.put(diff, line);
      
        if (closestLines.size() > limit) {
            closestLines.remove(closestLines.lastKey());
        }
        
        return 0;
    }

    protected int writeSorted(PrintWriter writer) {
        int count = 0;
        for (CDXLine line : closestLines.values()) {
            super.writeLine(writer, line);
            ++count;
        }
        closestLines.clear();
        return count;
    }

    @Override
    public void writeResumeKey(PrintWriter writer, String resumeKey) {
        writeSorted(writer);
        super.writeResumeKey(writer, resumeKey);
    }

    @Override
    public void end(PrintWriter writer) {
        writeSorted(writer);
        super.end(writer);
    }
}
