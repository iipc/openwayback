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

/**
 * Base class for implementations of CdxRecord.
 */
public abstract class BaseCdxRecord implements CdxRecord {

    final CdxRecordKey key;

    public final static CdxRecord create(final char[] value, final CdxFormat format) {
        if (format instanceof CdxLineFormat) {
            return new CdxLine(value, (CdxLineFormat) format);
        }

        throw new IllegalArgumentException("Unknow CdxFormat: " + format.getClass());
    }

    public final static CdxRecord create(final String value, final CdxFormat format) {
        return create(value.toCharArray(), format);
    }

    public BaseCdxRecord(final CdxRecordKey key) {
        this.key = key;
    }

    @Override
    public CdxRecordKey getKey() {
        return key;
    }

    @Override
    public String get(String fieldName) {
        return get(FieldName.forName(fieldName));
    }

    @Override
    public int compareTo(CdxRecord other) {
        return key.compareTo(other.getKey());
    }
}
