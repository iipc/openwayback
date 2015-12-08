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
 * Maps field indexes in requested output format to field indexes in input format.
 */
public class CdxLineFormatMapper {

    private final int[] outputFields;

    private final CdxLineSchema inputFormat;

    private final CdxLineSchema outputFormat;

    private int missingFieldCount = 0;

    /**
     * Constructor taking an input format and an output format and creates a mapping between them.
     * <p>
     * It is legal for the output format to be null in which case no mapping is done.
     * <p>
     * @param inputFormat the format of source of CDX lines.
     * @param outputFormat the requested output format.
     */
    public CdxLineFormatMapper(CdxLineSchema inputFormat, CdxLineSchema outputFormat) {

        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;

        if (outputFormat != null) {
            outputFields = new int[this.outputFormat.getLength()];
            for (int i = 0; i < outputFields.length; i++) {
                outputFields[i] = this.inputFormat.indexOf(this.outputFormat.getField(i));
                if (outputFields[i] == CdxLineSchema.MISSING_FIELD) {
                    outputFields[i] = - ++missingFieldCount;
                }
            }
        } else {
            outputFields = null;
        }
    }

    /**
     * Get the format used when parsing the CDX line.
     * <p>
     * @return the input format.
     */
    public CdxLineSchema getInputFormat() {
        return inputFormat;
    }

    /**
     * Get the format the CDX line will be converted to. Might be null if no conversion is
     * requested.
     * <p>
     * @return the output format.
     */
    public CdxLineSchema getOutputFormat() {
        return outputFormat;
    }

    /**
     * Get the field index in the input format for a given output field index.
     * <p>
     * @param outputFieldIndex the field index in the output format.
     * @return the field index in the input format.
     */
    public int getIndexOfOutputField(int outputFieldIndex) {
        return (outputFields[outputFieldIndex]);
    }

    public int getIndexOfOutputField(FieldDefinition outputField) {
        return (outputFields[outputFormat.indexOf(outputField)]);
    }

    public int getIndexOfOutputField(String outputFieldName) {
        return (outputFields[outputFormat.indexOf(outputFieldName)]);
    }

    /**
     * Get an array representing the output fields. Each element in the array refers to a field in
     * the input format.
     * <p>
     * @return the array of output fields.
     */
    public int[] getOutputFieldsIndexes() {
        return outputFields;
    }

    public int getMissingFieldCount() {
        return missingFieldCount;
    }
}
