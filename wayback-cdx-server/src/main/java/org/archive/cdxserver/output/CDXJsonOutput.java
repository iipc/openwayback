package org.archive.cdxserver.output;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.archive.format.cdx.CDXLine;

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

    @Override
    public void writeLine(PrintWriter writer, CDXLine line) {
        if (firstLine) {
            if (writeHeader) {
                writer.println(line.getNamesAsJson() + ",");
            }
            firstLine = false;
        } else {
            writer.println(',');
        }

        writer.print('[');

        boolean firstField = true;

        for (String field : line.fields) {
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
}
