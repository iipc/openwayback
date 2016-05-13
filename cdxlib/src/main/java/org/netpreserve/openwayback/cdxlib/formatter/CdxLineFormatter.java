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
import org.netpreserve.openwayback.cdxlib.json.NullValue;
import org.netpreserve.openwayback.cdxlib.json.Value;

/**
 *
 */
public class CdxLineFormatter implements CdxFormatter {

    @Override
    public void format(final Writer out, final CdxRecord<? extends CdxFormat> record,
            final CdxFormat outputFormat) throws IOException {

        CdxLineFormat format = (CdxLineFormat) outputFormat;

        out.write(record.getKey().toCharArray());

        for (int i = 2; i < format.getLength(); i++) {
            out.append(' ');
            Value value = record.get(format.getField(i));
            if (value == NullValue.NULL) {
                out.write('-');
            } else {
                out.write(record.get(format.getField(i)).toString());
            }
        }
    }

}
