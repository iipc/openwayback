package org.archive.cdxserver.output;

import java.io.PrintWriter;
import java.util.LinkedList;

import org.archive.format.cdx.CDXLine;

public class LastNLineOutput implements CDXOutput {
    
    protected CDXOutput output;
    protected LinkedList<CDXLine> lines;
    protected int limit = 1;
    
    public LastNLineOutput(CDXOutput output, int limit)
    {
        this.output = output;
        this.lines = new LinkedList<CDXLine>();
        this.limit = limit;
    }

    @Override
    public void begin(PrintWriter writer) {
        output.begin(writer);
    }

    @Override
    public int writeLine(PrintWriter writer, CDXLine line) {
        lines.add(line);
        
        if (lines.size() > limit) {
            lines.removeFirst();
        }
        
        return 0;
    }
    
    protected void flush(PrintWriter writer)
    {
        for (CDXLine line : lines) {
            output.writeLine(writer, line);
        }
        lines.clear();
    }

    @Override
    public void writeResumeKey(PrintWriter writer, String resumeKey) {
        if (!lines.isEmpty()) {
            flush(writer);
        }
        
        output.writeResumeKey(writer, resumeKey);
    }

    @Override
    public void end(PrintWriter writer) {
        if (!lines.isEmpty()) {
            flush(writer);
        }
        
        output.end(writer);
    }
}
