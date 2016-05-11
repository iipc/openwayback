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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of CdxFieldTypes defining a CDX Line format.
 */
public class CdxLineSchema {

    /**
     * Return value for fields not part of the schema.
     */
    public static final int MISSING_FIELD = -1;

    private final FieldName[] fields;

    private final char delimiter;

    /**
     * Matches space separated 11 column cdx format: ' CDX N b a m s k r M S V g'.
     */
    public static final CdxLineSchema CDX11LINE = new CdxLineSchema(' ',
            FieldName.URI_KEY,
            FieldName.TIMESTAMP,
            FieldName.ORIGINAL_URI,
            FieldName.MIME_TYPE,
            FieldName.RESPONSE_CODE,
            FieldName.DIGEST,
            FieldName.REDIRECT,
            FieldName.ROBOT_FLAGS,
            FieldName.LENGTH,
            FieldName.OFFSET,
            FieldName.FILENAME);

    /**
     * Matches space separated 9 column cdx format: ' CDX N b a m s k r V g'.
     */
    public static final CdxLineSchema CDX09LINE = new CdxLineSchema(' ',
            FieldName.URI_KEY,
            FieldName.TIMESTAMP,
            FieldName.ORIGINAL_URI,
            FieldName.MIME_TYPE,
            FieldName.RESPONSE_CODE,
            FieldName.DIGEST,
            FieldName.REDIRECT,
            FieldName.OFFSET,
            FieldName.FILENAME);

    /**
     * Constructor taking a list of field names as defined in
     * <a src="https://archive.org/web/researcher/cdx_legend.php">CDX and DAT Legend</a>.
     * <p>
     * @param delimiter the delimiter between fields in a line oriented cdx format.
     * @param fields ordered list of fields defining the CDX line format.
     */
    public CdxLineSchema(final char delimiter, final FieldName... fields) {
        this.delimiter = delimiter;
        this.fields = Arrays.copyOf(fields, fields.length);
    }

    /**
     * Constructs a new CdxLineSchema from a Cdx-format as specified in
     * <a href="http://iipc.github.io/warc-specifications/specifications/cdx-format/cdx-2015/">
     * http://iipc.github.io/warc-specifications/specifications/cdx-format/cdx-2015/</a>.
     * <p>
     * @param formatString the format string
     */
    public CdxLineSchema(final CharSequence formatString) {
        this.delimiter = formatString.charAt(0);
        List<FieldName> fieldList = new ArrayList<>(11);
        for (int i = 5; i < formatString.length(); i += 2) {
            fieldList.add(FieldName.forCode(formatString.charAt(i)));
        }
        this.fields = fieldList.toArray(new FieldName[0]);
    }

    /**
     * Constructs a new CdxLineSchema from a list of field names.
     * <p>
     * The field names are allowed to be one letter codes, long name representation for those codes
     * or user defined custom field names.
     * <p>
     * @param delimiter the delimiter between the fields used in CDX line representation
     * @param fieldNames the field names
     */
    public CdxLineSchema(final char delimiter, final List<String> fieldNames) {
        this(delimiter, fieldNames.toArray(new String[0]));
    }

    /**
     * Constructs a new CdxLineSchema from an array of field names.
     * <p>
     * @param delimiter the delimiter between the fields used in CDX line representation
     * @param fieldNames the field names
     */
    public CdxLineSchema(final char delimiter, final String... fieldNames) {
        this.delimiter = delimiter;
        this.fields = new FieldName[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            this.fields[i] = FieldName.forName(fieldNames[i]);
        }
    }

    /**
     * Get number of fields in the format.
     * <p>
     * @return number of fields.
     */
    public int getLength() {
        return fields.length;
    }

    /**
     * Get the index of a named field.
     * <p>
     * @param fieldName the field name to get an index for.
     * @return the index (zero based) or {@link #MISSING_FIELD} if not found.
     */
    public int indexOf(String fieldName) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(fieldName)) {
                return i;
            }
        }
        return MISSING_FIELD;
    }

    /**
     * Get the index of a field definition.
     * <p>
     * @param field the field name to get an index for.
     * @return the index (zero based) or {@link #MISSING_FIELD} if not found.
     */
    public int indexOf(FieldName field) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals(field)) {
                return i;
            }
        }
        return MISSING_FIELD;
    }

    /**
     * Get field name for the field at a particular position.
     * <p>
     * @param index the index to get the field definition for.
     * @return the field definition.
     * @throws IndexOutOfBoundsException if index &lt; 0 or &gt;= {@link #getLength()}.
     */
    public FieldName getField(int index) {
        return fields[index];
    }

    /**
     * Get the delimiter used between fields in a line oriented cdx format.
     * <p>
     * @return the delimiter.
     */
    public char getDelimiter() {
        return delimiter;
    }

}
