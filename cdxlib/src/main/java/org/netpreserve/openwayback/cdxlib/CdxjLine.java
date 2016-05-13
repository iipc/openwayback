/*
 * Copyright 2015 IIPC.
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
package org.netpreserve.openwayback.cdxlib;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.netpreserve.openwayback.cdxlib.formatter.CdxRecordFormatter;
import org.netpreserve.openwayback.cdxlib.json.SimpleJsonParser;
import org.netpreserve.openwayback.cdxlib.json.Value;

/**
 * A representation of a line in a CDX file.
 */
public class CdxjLine extends BaseCdxRecord<CdxjLineFormat> {

    final char[] data;
    Map<FieldName, Value> fields;

    public CdxjLine(final char[] key, final char[] value, CdxjLineFormat format) {
        super(new CdxRecordKey(key), format);
        this.data = value;
    }

    public CdxjLine(final char[] line, final CdxjLineFormat format) {
        super(getKeyFromLine(line), format);
        this.data = Arrays.copyOfRange(line, getKey().length() + 1, line.length);
    }

    @Override
    public Value get(FieldName fieldName) {
        return fields.get(fieldName);
    }

    @Override
    public boolean hasField(FieldName fieldName) {
        parseFields();
        return fields.containsKey(fieldName);
    }

    @Override
    public Iterator<Field> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void parseFields() {
        if (fields == null) {
            fields = new SimpleJsonParser(data, 0).parseObject();

            for (Map.Entry<FieldName, Value> e : fields.entrySet()) {
                System.out.println(e.getKey() + " = " + e.getValue().getClass());
            }

            CdxRecordFormatter formatter = new CdxRecordFormatter(getCdxFormat());
            System.out.println("Out: " + formatter.format(this));
        }
    }

//    @Override
//    public int hashCode() {
//        int hash = 1;
//        char[] array = data.array();
//        int start = data.arrayOffset();
//        int limit = start + fieldLengths[0] + fieldLengths[1];
//        for (int i = limit; i >= start; i--) {
//            hash = 31 * hash + (int) array[i];
//        }
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final CdxjLine other = (CdxjLine) obj;
//        if (!Objects.equals(this.data, other.data)) {
//            return false;
//        }
//        return true;
//    }

    @Override
    public char[] toCharArray() {
        return toString().toCharArray();
    }

    @Override
    public String toString() {
        return getKey().toString() + ' ' + String.copyValueOf(data);
    }

}
