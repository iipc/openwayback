package org.archive.cdxserver.output;

import java.io.PrintWriter;
import java.util.HashMap;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class CDXDupeCountWriter extends WrappedCDXOutput {

	protected HashMap<String, DupeTrack> dupeHashmap = null;
	
	public final static String dupecount = "dupecount";
	
	class DupeTrack
	{
		CDXLine line;
		int count;
		
		DupeTrack(CDXLine line)
		{
			this.line = line;
			count = 0;
		}
	}
	
	public CDXDupeCountWriter(CDXOutput output)
	{
		super(output);
		this.dupeHashmap = new HashMap<String, DupeTrack>();
	}
	
	@Override
	public boolean writeLine(PrintWriter writer, CDXLine line) {		
		String digest = line.getDigest();
		
		DupeTrack counter = dupeHashmap.get(digest);
		
		if (counter == null) {
			counter = new DupeTrack(line);
			dupeHashmap.put(digest, counter);
			line.setField(dupecount, "0");
		} else {
			if (line.getMimeType().equals("warc/revisit")) {
				line.setMimeType(counter.line.getMimeType());
			}
			counter.count++;
			line.setField(dupecount, "" + counter.count);
		}
		
		return inner.writeLine(writer, line);
	}
	
	@Override
	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
		return super.modifyOutputFormat(format).addFieldNames(dupecount);
	}	
}
