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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A representation of a line in a CDX file.
 */
public class CdxLine implements Comparable<CdxLine> {

    final CharBuffer data;

    final int[] fieldOffsets;

    final int[] fieldLengths;

    final CdxLineFormatMapper lineFormatMapper;

    int outLength;

    CharBuffer outLine;

    public CdxLine(final ByteBuffer line, final CdxLineFormatMapper lineFormat) {
        this(convertToChar(line), lineFormat);
    }

    public CdxLine(final CharBuffer line, final CdxLineFormatMapper lineFormat) {
        this.data = line;
        this.lineFormatMapper = lineFormat;

        final int inputFormatLength = this.lineFormatMapper.getInputFormat().getLength();
        this.fieldOffsets = new int[inputFormatLength];
        this.fieldLengths = new int[inputFormatLength];

        parseFields();
    }

    public CdxLine(final String line, final CdxLineFormatMapper lineFormat) {
        this(CharBuffer.wrap(line.toCharArray()), lineFormat);
    }

    public CharBuffer get(String fieldName) {
        int f = lineFormatMapper.getInputFormat().indexOf(fieldName);
        return data.subSequence(fieldOffsets[f], fieldOffsets[f] + fieldLengths[f]);
    }

    @Override
    public String toString() {
        return String.valueOf(getOutputLine());
    }

    public CharBuffer getInputLine() {
        return data;
    }

    public CharBuffer getOutputLine() {
        if (lineFormatMapper.getOutputFormat() == null) {
            return data;
        }

        char delimiter = lineFormatMapper.getOutputFormat().getDelimiter();
        int[] outputFieldIndexes = lineFormatMapper.getOutputFieldsIndexes();

        if (outLine == null) {
            // Calculate output array size
            outLength = outputFieldIndexes.length - 1;
            for (int f : outputFieldIndexes) {
                if (f == CdxLineSchema.MISSING_FIELD) {
                    // Field missing from input. Add room for '-'
                    outLength++;
                }
                outLength += fieldLengths[f];
            }

            char[] dataArray = this.data.array();
            char[] out = new char[outLength];
            int pos = 0;
            for (int f : outputFieldIndexes) {
                if (pos > 0) {
                    out[pos++] = delimiter;
                }

                if (f == CdxLineSchema.MISSING_FIELD) {
                    // Field missing from input. Add '-'
                    out[pos++] = '-';
                } else {
                    System.arraycopy(dataArray, fieldOffsets[f], out, pos, fieldLengths[f]);
                    pos += fieldLengths[f];
                }
            }
            outLine = CharBuffer.wrap(out);
        }

        return outLine;
    }

    public String getInputString() {
        return String.valueOf(data);
    }

    public int outLength() {
        return outLength;
    }

    public int inLength() {
        return data.length();
    }

    private static CharBuffer convertToChar(ByteBuffer line) {
        byte[] in = line.array();
        char[] out = new char[in.length];
        for (int i = 0; i < in.length; i++) {
            if (in[i] < 0) {
                return convertToCharNonAscii(line);
            }
            out[i] = (char) in[i];
        }
        return CharBuffer.wrap(out);
    }

    private static CharBuffer convertToCharNonAscii(ByteBuffer line) {
        return StandardCharsets.UTF_8.decode(line);
    }

    private void parseFields() {
        int fieldCount = 0;
        int lastIndex = 0;
        int currIndex;
        char delimiter = lineFormatMapper.getInputFormat().getDelimiter();

        do {
            currIndex = indexOf(delimiter, lastIndex);
            if (currIndex > 0) {
                fieldOffsets[fieldCount] = lastIndex;
                fieldLengths[fieldCount] = currIndex - lastIndex;
            } else {
                fieldOffsets[fieldCount] = lastIndex;
                fieldLengths[fieldCount] = data.length() - lastIndex;
                break;
            }
            lastIndex = currIndex + 1;
            fieldCount++;
        } while (lastIndex > 0);
    }

    private int indexOf(char ch, int fromIndex) {
        final int max = data.length();
        fromIndex += data.arrayOffset();
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        final char[] value = this.data.array();
        for (int i = fromIndex; i < max; i++) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int compareTo(CdxLine other) {
        char[] data1 = data.array();
        int offset1 = data.arrayOffset();
        char[] data2 = other.data.array();
        int offset2 = other.data.arrayOffset();
        int len1 = fieldLengths[0] + fieldLengths[1] + 1;
        int len2 = other.fieldLengths[0] + other.fieldLengths[1] + 1;
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim) {
            char c1 = data1[k + offset1];
            char c2 = data2[k + offset2];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        char[] array = data.array();
        int start = data.arrayOffset();
        int limit = start + fieldLengths[0] + fieldLengths[1];
        for (int i = limit; i >= start; i--) {
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
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

}
