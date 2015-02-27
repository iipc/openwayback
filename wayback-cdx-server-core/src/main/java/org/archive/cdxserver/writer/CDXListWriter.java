package org.archive.cdxserver.writer;

import java.util.ArrayList;

import org.archive.format.cdx.CDXLine;

public class CDXListWriter extends CDXWriter {

	protected ArrayList<CDXLine> cdxLines;
	
	public CDXListWriter()
	{
		cdxLines = new ArrayList<CDXLine>();
	}
	
	@Override
    public void begin() {
	    // TODO Auto-generated method stub
    }

	@Override
    public int writeLine(CDXLine line) {
		cdxLines.add(line);
		return 1;
    }

	@Override
    public void writeResumeKey(String resumeKey) {
		
    }

	@Override
    public void end() {
	    
    }
	
	public ArrayList<CDXLine> getCDXLines()
	{
		return cdxLines;
	}
}
