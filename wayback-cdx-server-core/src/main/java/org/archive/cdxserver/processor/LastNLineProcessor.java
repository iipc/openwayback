package org.archive.cdxserver.processor;

import java.util.LinkedList;

import org.archive.format.cdx.CDXLine;

public class LastNLineProcessor extends WrappedProcessor {
    
    protected LinkedList<CDXLine> lines;
    protected int limit = 1;
    
    public LastNLineProcessor(BaseProcessor output, int limit)
    {
        super(output);
        this.lines = new LinkedList<CDXLine>();
        this.limit = limit;
    }

    @Override
    public int writeLine(CDXLine line) {
        lines.add(line);
        
        if (lines.size() > limit) {
            lines.removeFirst();
        }
        
        return 0;
    }
    
    protected void flush()
    {
        for (CDXLine line : lines) {
            inner.writeLine(line);
        }
        lines.clear();
    }

    @Override
    public void writeResumeKey(String resumeKey) {
        if (!lines.isEmpty()) {
            flush();
        }
        
        inner.writeResumeKey(resumeKey);
    }

    @Override
    public void end() {
        if (!lines.isEmpty()) {
            flush();
        }
        
        inner.end();
    }
}
