/*
 * Copyright 2016 IIPC.
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
package org.netpreserve.openwayback.cdxlib.formatter;

import java.io.IOException;
import java.io.Writer;

import org.netpreserve.openwayback.cdxlib.CdxFormat;
import org.netpreserve.openwayback.cdxlib.CdxLineFormat;
import org.netpreserve.openwayback.cdxlib.CdxRecord;
import org.netpreserve.openwayback.cdxlib.CdxRecordKey;
import org.netpreserve.openwayback.cdxlib.CdxjLineFormat;
import org.netpreserve.openwayback.cdxlib.FieldName;
import org.netpreserve.openwayback.cdxlib.SearchResult;

/**
 *
 */
public class CdxRecordFormatter {

    private static final CdxLineFormatter CDX_LINE_FORMATTER = new CdxLineFormatter();

    private static final CdxjLineFormatter CDXJ_LINE_FORMATTER = new CdxjLineFormatter();

    private final CdxFormat format;

    private final CdxFormatter formatter;

    public CdxRecordFormatter(CdxFormat format) {
        this.format = format;
        if (format instanceof CdxLineFormat) {
            formatter = CDX_LINE_FORMATTER;
        } else if (format instanceof CdxjLineFormat) {
            formatter = CDXJ_LINE_FORMATTER;
        } else {
            throw new IllegalArgumentException("No formatter for format: " + format.getClass());
        }
    }

    public String format(final CdxRecord record) {
        return format(record, false);
    }

    public String format(final CdxRecord record, final boolean createKey) {
        if (createKey) {
            CdxRecordKey key = new CdxRecordKey(record.get(FieldName.ORIGINAL_URI),
                    record.get(FieldName.TIMESTAMP));
            record.setKey(key);
        }

        if (record.getCdxFormat().equals(format)) {
            return record.toString();
        }

        StringBuilderWriter sbw = new StringBuilderWriter();
        try {
            formatter.format(sbw, record, format);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return sbw.toString();
    }

    public void format(final Writer out, final CdxRecord record, final boolean createKey) throws IOException {
        if (createKey) {
            CdxRecordKey key = new CdxRecordKey(record.get(FieldName.ORIGINAL_URI),
                    record.get(FieldName.TIMESTAMP));
            record.setKey(key);
        }

        if (record.getCdxFormat().equals(format)) {
            out.write(record.toCharArray());
            return;
        }

        try {
            formatter.format(out, record, format);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void format(final Writer out, final SearchResult searchResult, final long limit)
            throws IOException {

        long count = 0;
        for (CdxRecord cdxLine : searchResult) {
            if (count >= limit) {
                break;
            }
            count++;

            format(out, cdxLine, false);
            out.append('\n');
        }

    }
}
