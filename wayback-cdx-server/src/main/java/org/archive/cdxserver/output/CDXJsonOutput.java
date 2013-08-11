package org.archive.cdxserver.output;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class CDXJsonOutput implements CDXOutput {

    boolean writeHeader = true;
    boolean firstLine = true;

    public CDXJsonOutput() {

    }

    @Override
    public void begin(PrintWriter writer) {
        firstLine = true;
        writer.print('[');
    }
    
    protected void writeHeader(PrintWriter writer, FieldSplitFormat names)
    {
  		if (names == null || names.getLength() == 0) {
  			writer.print("[]");
  			return;
  		}

  		writer.print('[');

  		for (int i = 0; i < names.getLength(); i++) {
  			if (i > 0) {
  				writer.print(',');
  			}
  			writer.print('\"');
  			writer.print(names.getName(i));
  			writer.print('\"');
  		}

  		writer.print(']');
    }

    @Override
    public int writeLine(PrintWriter writer, CDXLine line) {
        if (firstLine) {
            if (writeHeader) {
            	writeHeader(writer, line.getNames());
                writer.println(',');
            }
            firstLine = false;
        } else {
            writer.println(',');
        }

        writer.print('[');

        boolean firstField = true;

        for (int i = 0; i < line.getNumFields(); i++) {
          String field = line.getField(i);
            if (firstField) {
                writer.print('\"');
                firstField = false;
            } else {
                writer.print("\", \"");
            }
            writer.print(StringEscapeUtils.escapeJava(field));
        }

        if (!firstField) {
            writer.print('\"');
        }

        writer.print(']');
        return 1;
    }

    @Override
    public void end(PrintWriter writer) {
        writer.println(']');
    }

    @Override
    public void writeResumeKey(PrintWriter writer, String resumeKey) {
        writer.println(",");
        writer.println("[],");
        writer.print("[\"");
        writer.print(resumeKey);
        writer.print("\"]");
    }

	@Override
	public void trackLine(CDXLine line) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
		return format;
	}
}
