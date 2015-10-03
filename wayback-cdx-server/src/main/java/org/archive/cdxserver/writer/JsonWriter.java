package org.archive.cdxserver.writer;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

public class JsonWriter extends HttpCDXWriter {

    boolean writeHeader = true;
    boolean firstLine = true;

    public JsonWriter(HttpServletResponse response, boolean gzip) throws IOException {
    	super(response, gzip);
		setContentType("application/json");
    }

    @Override
    public void begin() {
        firstLine = true;
        writer.print('[');
    }
    
    protected void writeHeader(FieldSplitFormat names)
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
    public int writeLine(CDXLine line) {
		if (!includeBlockedCaptures && isBlocked(line))
			return 0;
        if (firstLine) {
            if (writeHeader) {
            	writeHeader(line.getNames());
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
    public void end() {
        writer.println(']');
    }

    @Override
    public void writeResumeKey(String resumeKey) {
        writer.println(",");
        writer.println("[],");
        writer.print("[\"");
        writer.print(resumeKey);
        writer.print("\"]");
    }
}
