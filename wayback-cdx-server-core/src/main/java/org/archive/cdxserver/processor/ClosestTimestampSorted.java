package org.archive.cdxserver.processor;

import java.util.Date;
import java.util.TreeMap;

import org.archive.format.cdx.CDXLine;
import org.archive.util.ArchiveUtils;

public class ClosestTimestampSorted extends WrappedProcessor {
    
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
    
    public ClosestTimestampSorted(BaseProcessor output, String target, int limit) {
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
    public int writeLine(CDXLine line) {
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
                return writeSorted();
            }
        }
        
        closestLines.put(diff, line);
      
        if (closestLines.size() > limit) {
            closestLines.remove(closestLines.lastKey());
        }
        
        return 0;
    }

    protected int writeSorted() {
        int count = 0;
        for (CDXLine line : closestLines.values()) {
            super.writeLine(line);
            ++count;
        }
        closestLines.clear();
        return count;
    }

    @Override
    public void writeResumeKey(String resumeKey) {
        writeSorted();
        super.writeResumeKey(resumeKey);
    }

    @Override
    public void end() {
        writeSorted();
        super.end();
    }
}
