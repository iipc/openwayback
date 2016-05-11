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

/**
 *
 */
public interface CdxRecord extends Comparable<CdxRecord> {

    /**
     * Get the key for this record.
     *
     * @return the key or null if no one exist
     */
    CdxRecordKey getKey();

    /**
     * Convenience method to get a named field by its name as a String.
     * <p>
     * @param fieldName the name of the requested field
     * @return the field value
     * @see #get(org.netpreserve.openwayback.cdxlib.FieldName) 
     */
    String get(String fieldName);

    /**
     * Get a named field.
     * <p>
     * @param fieldName the name of the requested field
     * @return the field value
     */
    String get(FieldName fieldName);

    /**
     * Compares this object with the specified LegacyCdxLine for order. Returns a negative integer,
     * zero, or a positive integer as this LegacyCdxLine is less than, equal to, or greater than the
     * specified LegacyCdxLine.
     * <p>
     * Note: This method uses only the key in the LegacyCdxLine for determining the
     * natural order. It is expected that the key fields are the url key and timestamp which in
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
    int compareTo(CdxRecord other);
}
