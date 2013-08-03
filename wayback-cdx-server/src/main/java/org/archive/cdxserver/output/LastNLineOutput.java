package org.archive.cdxserver.output;

import java.io.PrintWriter;
import java.util.LinkedList;

import org.archive.format.cdx.CDXLine;

public class LastNLineOutput extends WrappedCDXOutput {
    
    protected LinkedList<CDXLine> lines;
    protected int limit = 1;
    
    public LastNLineOutput(CDXOutput output, int limit)
    {
        super(output);
        this.lines = new LinkedList<CDXLine>();
        this.limit = limit;
    }

    @Override
    public boolean writeLine(PrintWriter writer, CDXLine line) {
        lines.add(line);
        
        if (lines.size() > limit) {
            lines.removeFirst();
        }
        
        return false;
    }
    
    protected void flush(PrintWriter writer)
    {
        for (CDXLine line : lines) {
            inner.writeLine(writer, line);
        }
        lines.clear();
    }

    @Override
    public void writeResumeKey(PrintWriter writer, String resumeKey) {
        if (!lines.isEmpty()) {
            flush(writer);
        }
        
        inner.writeResumeKey(writer, resumeKey);
    }

    @Override
    public void end(PrintWriter writer) {
        if (!lines.isEmpty()) {
            flush(writer);
        }
        
        inner.end(writer);
    }
}
