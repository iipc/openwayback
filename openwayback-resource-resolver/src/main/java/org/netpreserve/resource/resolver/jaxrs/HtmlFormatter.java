/*
 * Copyright 2016 The International Internet Preservation Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netpreserve.resource.resolver.jaxrs;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;

import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxRecordKey;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.formatter.CdxFormatter;

/**
 *
 */
public class HtmlFormatter implements CdxFormatter {

    private static final char[] JSON_START = " {".toCharArray();

    private static final char JSON_END = '}';

    private static final char COMMA = ',';

    private static final String FIELD_NAME_START = "\"<span class='fieldName'>";

    private static final char[] FIELD_NAME_END = "</span>\":".toCharArray();

    @Override
    public void format(Writer out, CdxRecord record, CdxFormat outputFormat) throws IOException {

        CdxRecordKey key = record.getKey();
        out.write("<span class='uriKey'>");
        out.write(key.getUriKey().getValue());
        out.write("</span> <span class='timeStamp'>");
        out.write(key.getTimeStamp().getValue().toFormattedString(outputFormat.getKeyDateFormat()));
        out.write("</span> <span class='recordType'>");
        out.write(key.getRecordType().getValue());
        out.write("</span>");

        out.write(JSON_START);

        boolean notFirst = false;
        for (CdxRecord.Field entry : record) {
            if (!entry.getFieldName().equals(FieldName.URI_KEY)
                    && !entry.getFieldName().equals(FieldName.TIMESTAMP)
                    && !entry.getFieldName().equals(FieldName.FILENAME)
                    && !entry.getFieldName().equals(FieldName.OFFSET)
                    && !entry.getFieldName().equals(FieldName.RECORD_TYPE)) {

                if (notFirst) {
                    out.write(COMMA);
                }

                FieldName name = entry.getFieldName();

                out.write(FIELD_NAME_START);
                out.write(name.getName());
                out.write(FIELD_NAME_END);

                switch (entry.getFieldName().getName()) {
                    case "rou":
                        out.write("<a href=\"/resource/"
                                + URLEncoder.encode(record.get(FieldName.REVISIT_ORIGINAL_URI).toString(), "UTF-8")
                                + "/" + record.get(FieldName.REVISIT_ORIGINAL_DATE)
                                + "?type=" + record.getKey().getRecordType().toString());
                        out.write("\">");
                        entry.getValue().toJson(out);
                        out.write("</a>");
                        break;
                    case "uri":
                        out.write("<a href=\"/resource/"
                                + URLEncoder.encode(record.get(FieldName.ORIGINAL_URI).toString(), "UTF-8")
                                + "/" + record.getKey().getTimeStamp()
                                + "?type=" + record.getKey().getRecordType().toString());
                        out.write("\">");
                        entry.getValue().toJson(out);
                        out.write("</a>");
                        break;
                    default:
                        entry.getValue().toJson(out);
                }

                notFirst = true;
            }
        }

        if (!record.hasField(FieldName.RESOURCE_REF)) {
            if (notFirst) {
                out.write(COMMA);
            }
            out.write(FIELD_NAME_START);
            out.write(FieldName.RESOURCE_REF.getName());
            out.write(FIELD_NAME_END);
            out.write("\"warcfile:");
            out.write(record.get(FieldName.FILENAME).toString());
            out.write('#');
            out.write(record.get(FieldName.OFFSET).toString());
            out.write('"');
        }

        out.write(JSON_END);
    }

}
