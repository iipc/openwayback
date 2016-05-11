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
package org.netpreserve.openwayback.cdxlib;

import java.util.Objects;

/**
 * Representation of the key used to lookup records in CDX files.
 * <p>
 * A CDX record key is composed of a canonicalized uri in SURT form, a space and a 14-17 digit time
 * stamp.
 */
public class CdxRecordKey implements Comparable<CdxRecordKey> {

    private char[] data;

    private String uriKey;

    private String timeStamp;

    /**
     * Construct a new CdxRecordKey from a char array.
     * <p>
     * @param data the complete key as a char array
     */
    public CdxRecordKey(final char[] data) {
        this.data = Objects.requireNonNull(data);
    }

    public CdxRecordKey(final String data) {
        this.data = Objects.requireNonNull(data).toCharArray();
    }

    public CdxRecordKey(final String uriKey, final String timeStamp) {
        this.uriKey = Objects.requireNonNull(uriKey);
        this.timeStamp = Objects.requireNonNull(timeStamp);
    }

    public String getUriKey() {
        if (uriKey == null) {
            parse();
        }
        return uriKey;
    }

    public String getTimeStamp() {
        if (timeStamp == null) {
            parse();
        }
        return timeStamp;

    }

    /**
     * Compares this object with the specified LegacyCdxLine for order. Returns a negative integer,
     * zero, or a positive integer as this LegacyCdxLine is less than, equal to, or greater than the
     * specified LegacyCdxLine.
     * <p>
     * Note: This method uses only the first two fields in the LegacyCdxLine for determining the
     * natural order. It is expected that those fields are the url key and timestamp which in
     * general uniquely identifies the line. In contrast the {@link #equals(java.lang.Object)}
     * method compares the whole line. It is then possible that (x.compareTo(y)==0) == (x.equals(y))
     * is not always true, but it usually is when comparing CdxLines with the same number of input
     * fields.
     * <p>
     * @param other the LegacyCdxLine to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal
     * to, or greater than the specified object
     * @throws NullPointerException if the specified object is null
     */
    @Override
    public int compareTo(CdxRecordKey other) {
        char[] thisData = toCharArray();
        char[] otherData = other.toCharArray();
        int lim = Math.min(thisData.length, otherData.length);

        int k = 0;
        while (k < lim) {
            char c1 = thisData[k];
            char c2 = otherData[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return thisData.length - otherData.length;
    }

    @Override
    public String toString() {
        if (data != null) {
            return new String(data);
        } else {
            return uriKey + ' ' + timeStamp;
        }
    }

    public char[] toCharArray() {
        if (data == null) {
            data = (uriKey + ' ' + timeStamp).toCharArray();
        }
        return data;
    }

    /**
     * Parse the char array into its components.
     */
    private void parse() {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == ' ') {
                uriKey = new String(data, 0, i);
                timeStamp = new String(data, i + 1, data.length - i - 1);
                return;
            }
        }
        throw new IllegalArgumentException("The CDX record key '" + new String(data)
                + "' cannot be parsed");
    }

}
