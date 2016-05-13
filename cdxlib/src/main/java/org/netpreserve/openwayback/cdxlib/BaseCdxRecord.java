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
import java.util.NoSuchElementException;

import org.netpreserve.openwayback.cdxlib.json.Value;

/**
 * Base class for implementations of CdxRecord.
 * <p>
 * @param <T>
 */
public abstract class BaseCdxRecord<T extends CdxFormat> implements CdxRecord<T> {

    private final T format;

    private CdxRecordKey key;

    protected boolean modified = false;

    public final static CdxRecord create(final char[] value, final CdxFormat format) {
        if (format instanceof CdxLineFormat) {
            return new CdxLine(value, (CdxLineFormat) format);
        } else if (format instanceof CdxjLineFormat) {
            return new CdxjLine(value, (CdxjLineFormat) format);
        }

        throw new IllegalArgumentException("Unknow CdxFormat: " + format.getClass());
    }

    public final static CdxRecord create(final String value, final CdxFormat format) {
        return create(value.toCharArray(), format);
    }

    public BaseCdxRecord(final T format) {
        this.format = format;
    }

    public BaseCdxRecord(final CdxRecordKey key, final T format) {
        this.format = format;
        this.key = key;
    }

    @Override
    public T getCdxFormat() {
        return format;
    }

    @Override
    public CdxRecordKey getKey() {
        return key;
    }

    @Override
    public void setKey(CdxRecordKey recordKey) {
        this.key = recordKey;
        this.modified = true;
    }

    @Override
    public Value get(String fieldName) {
        return get(FieldName.forName(fieldName));
    }

    @Override
    public int compareTo(CdxRecord other) {
        return key.compareTo(other.getKey());
    }

    /**
     * Helper method to extract the first two fields from a line based CDX format.
     * <p>
     * @param line the line containing the key
     * @return a CdxRecordKey with the parsed values
     */
    protected static CdxRecordKey getKeyFromLine(final char[] line) {
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
     * Helper method to find the index of a character in a char array.
     * <p>
     * @param src the array to search
     * @param ch the char to look for
     * @param fromIndex where in the src to start. If &lt;= 0, the beggining of the array is
     * assumed. If &gt;= src.length, then -1 is returned.
     * @return the index of the first occurence of ch or -1 if not found.
     */
    protected static int indexOf(char[] src, char ch, int fromIndex) {
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

    protected abstract class FieldIterator implements Iterator<CdxRecord.Field> {

        private CdxRecord.Field next;

        @Override
        public boolean hasNext() {
            if (next == null) {
                next = getNext();
            }
            if (next == null) {
                return false;
            }
            return true;
        }

        @Override
        public CdxRecord.Field next() {
            if (hasNext()) {
                CdxRecord.Field result = next;
                next = null;
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected abstract CdxRecord.Field getNext();

    }

    protected static class ImmutableField implements Field {

        private final FieldName name;

        private final Value value;

        public ImmutableField(FieldName name, Value value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public FieldName getFieldName() {
            return name;
        }

        @Override
        public Value getValue() {
            return value;
        }

    }
}
