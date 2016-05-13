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

import org.netpreserve.openwayback.cdxlib.json.NullValue;
import org.netpreserve.openwayback.cdxlib.json.NumberValue;
import org.netpreserve.openwayback.cdxlib.json.StringValue;
import org.netpreserve.openwayback.cdxlib.json.Value;

/**
 * A representation of a line in a legacy CDX file.
 */
public class CdxLine extends BaseCdxRecord<CdxLineFormat> {

    private static final char EMPTY_FIELD_VALUE = '-';

    final char[] data;

    private int[] fieldOffsets;

    private int[] fieldLengths;

    private transient Value[] fieldCache;

    public CdxLine(final String line, final CdxLineFormat format) {
        this(line.toCharArray(), format);
    }

    public CdxLine(final char[] line, final CdxLineFormat format) {
        super(getKeyFromLine(line), format);
        this.data = line;

        parseFields();
    }

    @Override
    public Value get(FieldName fieldName) {
        return getValue(fieldName, getCdxFormat().indexOf(fieldName));
    }

    /**
     * Get the field at a certain position in the input line.
     * <p>
     * @param fieldIndex the index of the field requested
     * @return the fields value
     */
    public Value get(int fieldIndex) {
        if (fieldIndex < 0 || fieldIndex >= fieldOffsets.length) {
            throw new IllegalArgumentException("No such field");
        }
        return getValue(getCdxFormat().getField(fieldIndex), fieldIndex);
//        return String.copyValueOf(data, fieldOffsets[fieldIndex], fieldLengths[fieldIndex]);
    }

    private Value getValue(FieldName name, int fieldIndex) {
        if (fieldCache == null) {
            fieldCache = new Value[getCdxFormat().getLength()];
        }
        if (fieldCache[fieldIndex] != null) {
            return fieldCache[fieldIndex];
        }

        Value result;
        if (data[fieldOffsets[fieldIndex]] == EMPTY_FIELD_VALUE) {
            result = NullValue.NULL;
        } else {
            switch (name.getType()) {
                case STRING:
                    result = StringValue
                            .valueOf(data, fieldOffsets[fieldIndex], fieldOffsets[fieldIndex] + fieldLengths[fieldIndex]);
                    break;
                case NUMBER:
                    result = NumberValue
                            .valueOf(data, fieldOffsets[fieldIndex], fieldOffsets[fieldIndex] + fieldLengths[fieldIndex]);
                    break;
                default:
                    result = NullValue.NULL;
                    break;
            }
        }
        fieldCache[fieldIndex] = result;
        return result;
    }

    @Override
    public boolean hasField(FieldName fieldName) {
        return getCdxFormat().indexOf(fieldName) != CdxLineFormat.MISSING_FIELD;
    }

    /**
     * Gets the data represented by this object.
     * <p>
     * @return the output data
     */
    @Override
    public String toString() {
        if (modified) {
            StringBuilder sb = new StringBuilder(data.length);
            sb.append(getKey().toString());
            for (int i = 2; i < getCdxFormat().getLength(); i++) {
                sb.append(' ');
                sb.append(get(i));
            }
            return sb.toString();
        } else {
            return String.valueOf(data);
        }
    }

    @Override
    public char[] toCharArray() {
        if (modified) {
            StringBuilder sb = new StringBuilder(data.length);
            sb.append(getKey().toString());
            for (int i = 2; i < getCdxFormat().getLength(); i++) {
                sb.append(' ');
                sb.append(get(i));
            }
            char[] result = new char[sb.length()];
            sb.getChars(0, sb.length(), result, 0);
            return result;
        } else {
            return data;
        }
    }

    @Override
    public Iterator<Field> iterator() {
        return new FieldIterator() {
            int fieldIdx = 0;

            CdxLineFormat format = getCdxFormat();

            int length = format.getLength();

            @Override
            protected Field getNext() {
                while (fieldIdx < length) {
                    Value value = get(fieldIdx);
                    if (value != NullValue.NULL) {
                        FieldName name = format.getField(fieldIdx);
                        fieldIdx++;
                        return new ImmutableField(name, value);
                    } else {
                        fieldIdx++;
                    }
                }
                return null;
            }

        };
    }

    private void parseFields() {
        int fieldCount = 0;
        int lastIndex = 0;
        int currIndex;
        char delimiter = getCdxFormat().getDelimiter();
        fieldOffsets = new int[getCdxFormat().getLength()];
        fieldLengths = new int[getCdxFormat().getLength()];

        do {
            currIndex = indexOf(data, delimiter, lastIndex);
            if (currIndex > 0) {
                fieldOffsets[fieldCount] = lastIndex;
                fieldLengths[fieldCount] = currIndex - lastIndex;
            } else {
                fieldOffsets[fieldCount] = lastIndex;
                fieldLengths[fieldCount] = data.length - lastIndex;
                break;
            }
            lastIndex = currIndex + 1;
            fieldCount++;
        } while (lastIndex > 0);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        char[] array = data;
        int limit = fieldLengths[0] + fieldLengths[1];
        for (int i = limit; i >= 0; i--) {
            hash = 31 * hash + (int) array[i];
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CdxLine other = (CdxLine) obj;
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

}
