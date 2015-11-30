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
import java.util.List;

/**
 * A collection of CdxFieldTypes defining a CDX Line format.
 */
public class CdxLineSchema {

    public static final int MISSING_FIELD = -1;

    private final FieldDefinition[] fields;

    private final char delimiter;

    /**
     * Matches space separated 11 column cdx format: ' CDX N b a m s k r M S V g'.
     */
    public static final CdxLineSchema CDX11LINE = new CdxLineSchema(' ',
            FieldType.N,
            FieldType.b,
            FieldType.a,
            FieldType.m,
            FieldType.s,
            FieldType.k,
            FieldType.r,
            FieldType.M,
            FieldType.S,
            FieldType.V,
            FieldType.g);

    /**
     * Matches space separated 9 column cdx format: ' CDX N b a m s k r V g'.
     */
    public static final CdxLineSchema CDX09LINE = new CdxLineSchema(' ',
            FieldType.N,
            FieldType.b,
            FieldType.a,
            FieldType.m,
            FieldType.s,
            FieldType.k,
            FieldType.r,
            FieldType.V,
            FieldType.g);

    /**
     * Constructor taking a list of field types as defined in
     * <a src="https://archive.org/web/researcher/cdx_legend.php">CDX and DAT Legend</a>.
     * <p>
     * @param delimiter the delimiter between fields in a line oriented cdx format.
     * @param fields ordered list of fields defining the CDX line format.
     */
    public CdxLineSchema(final char delimiter, final FieldType... fields) {
        this.delimiter = delimiter;
        this.fields = new FieldDefinition[fields.length];
        for (int i = 0; i < fields.length; i++) {
            this.fields[i] = new FieldDefinition(fields[i]);
        }
    }

    /**
     * Constructor taking a list of {@link FieldDefinition} objects.
     * <p>
 A FieldDefinition object is a wrapper around {@link FieldType} to allow custom defined
     * field types not part of the
     * <a src="https://archive.org/web/researcher/cdx_file_format.php">CDX specification</a>. A
     * custom field can only be used in CDX formats other than the original line oriented format
     * e.g. CDXJ.
     * <p>
     * @param delimiter the delimiter between fields in a line oriented cdx format.
     * @param fields ordered list of fields defining the CDX line format.
     */
    public CdxLineSchema(final char delimiter, final FieldDefinition... fields) {
        this.delimiter = delimiter;
        this.fields = fields;
    }

    public CdxLineSchema(final CharSequence formatString) {
        this.delimiter = formatString.charAt(0);
        List<FieldDefinition> fieldList = new ArrayList<>(11);
        for (int i = 5; i < formatString.length(); i += 2) {
            fieldList.add(new FieldDefinition(FieldType.forCode(formatString.charAt(i))));
        }
        this.fields = fieldList.toArray(new FieldDefinition[0]);
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
//        throw new IllegalArgumentException("Found no field with name " + fieldName);
    }

    /**
     * Get the index of a field definition.
     * <p>
     * @param field the field definition to get an index for.
     * @return the index (zero based) or {@link #MISSING_FIELD} if not found.
     */
    public int indexOf(FieldDefinition field) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals(field)) {
                return i;
            }
        }
        return MISSING_FIELD;
//        throw new IllegalArgumentException("Found no field " + field);
    }

    /**
     * Get the name of the field at a particular position.
     * <p>
     * @param index the index to get the field name for.
     * @return the field name.
     * @throws IndexOutOfBoundsException if index &lt; 0 or &gt;= {@link #getLength()}.
     */
    public String getName(int index) {
        return fields[index].getName();
    }

    /**
     * Get field definition for the field at a particular position.
     * <p>
     * @param index the index to get the field definition for.
     * @return the field definition.
     * @throws IndexOutOfBoundsException if index &lt; 0 or &gt;= {@link #getLength()}.
     */
    public FieldDefinition getField(int index) {
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
