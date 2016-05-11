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

/**
 * A representation of a line in a legacy CDX file.
 */
public class CdxLine extends BaseCdxRecord {

    private static final String EMPTY_FIELD_VALUE = "-";

    final char[] data;

    private int[] fieldOffsets;

    private int[] fieldLengths;

    private final CdxLineFormat lineFormat;

    public CdxLine(final String line, final CdxLineFormat lineFormat) {
        this(line.toCharArray(), lineFormat);
    }

    public CdxLine(final char[] line, final CdxLineFormat lineFormat) {
        super(getKeyFromLine(line));
        this.data = line;
        this.lineFormat = lineFormat;

        parseFields();
    }

    private static CdxRecordKey getKeyFromLine(final char[] line) {
        int indexOfSecondField = indexOf(line, ' ', 0);
        if (indexOfSecondField > 0) {
            int indexOfThirdField = indexOf(line, ' ', indexOfSecondField + 1);
            if (indexOfThirdField > 0) {
                return new CdxRecordKey(Arrays.copyOf(line, indexOfThirdField));
            } else if (line.length > indexOfSecondField + 1) {
                return new CdxRecordKey(line);
            }
        }

        throw new IllegalArgumentException("The CDX record '" + new String(line)
                + "' cannot be parsed");
    }

    /**
     * Get the first field with a certain name.
     * <p>
     * This method gets fields from the input. To get new mutable fields added during processing,
     * use {@link #getMutableField(java.lang.String)}.
     * <p>
     * @param fieldName the name of the requested field
     * @return the CharBuffer with the fields value
     * @see #get(org.netpreserve.openwayback.cdxlib.FieldType)
     * @see #get(int)
     * @see #getMutableField(java.lang.String)
     */
    @Override
    public String get(FieldName fieldName) {
        int f = lineFormat.indexOf(fieldName);
        return get(f);
    }

    /**
     * Get the field at a certain position in the input line.
     * <p>
     * This method gets fields from the input. To get new mutable fields added during processing,
     * use {@link #getMutableField(java.lang.String)}.
     * <p>
     * @param fieldIndex the index of the field requested
     * @return the CharBuffer with the fields value
     * @see #get(java.lang.String)
     * @see #get(org.netpreserve.openwayback.cdxlib.FieldType)
     * @see #getMutableField(java.lang.String)
     */
    private String get(int fieldIndex) {
        if (fieldIndex < 0 || fieldIndex >= fieldOffsets.length) {
            throw new IllegalArgumentException("No such field");
        }
        return String.copyValueOf(data, fieldOffsets[fieldIndex], fieldLengths[fieldIndex]);
    }

    /**
     * Gets the data represented by this object after transforming it to the output format.
     * <p>
     * @return the output data
     */
    @Override
    public String toString() {
        return String.valueOf(data);
    }

    private void parseFields() {
        int fieldCount = 0;
        int lastIndex = 0;
        int currIndex;
        char delimiter = lineFormat.getDelimiter();
        fieldOffsets = new int[lineFormat.getLength()];
        fieldLengths = new int[lineFormat.getLength()];

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

    private static int indexOf(char[] src, char ch, int fromIndex) {
        final int max = src.length;
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        final char[] value = src;
        for (int i = fromIndex; i < max; i++) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
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
