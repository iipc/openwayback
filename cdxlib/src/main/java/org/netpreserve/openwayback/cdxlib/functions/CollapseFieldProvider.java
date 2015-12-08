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
package org.netpreserve.openwayback.cdxlib.functions;

import java.nio.CharBuffer;

import org.netpreserve.openwayback.cdxlib.CdxLine;

/**
 * Class for constructing CollapseField functions.
 */
public class CollapseFieldProvider implements
        FunctionProvider<CollapseFieldProvider.CollapseField> {

    private final String collapseString;

    /**
     * Constructs a new CollapseFieldProvider using a collapse description for creating new Collapse
     * Field functions.
     * <p>
     * @param collapseString the collapse description
     */
    public CollapseFieldProvider(String collapseString) {
        this.collapseString = collapseString;
    }

    @Override
    public CollapseField newCdxFunction() {
        return new CollapseField(collapseString);
    }

    /**
     * Deduplicates a CdxLine by a specific field, or part of a specific field.
     * <p>
     * eg:
     * <p>
     * {@code <field>} = if {@code <field>} matches previous match, then its a duplicate
     * {@code <field>:<n>} = if first {@code <n>} character of {@code <field>} match, then its a
     * dupe
     * <p>
     */
    public static class CollapseField implements BiFunction {

        private static final String FIELD_SEP_CHAR = ":";

        private final String fieldName;

        private final int substrLength;

        private boolean isCollapsing = false;

        public CollapseField(String collapseString) {
            int sepIndex = collapseString.indexOf(FIELD_SEP_CHAR);

            // Match entire field
            if (sepIndex < 0) {
                fieldName = collapseString;
                substrLength = -1;
            } else {
                fieldName = collapseString.substring(0, sepIndex);
                substrLength = Integer.parseInt(collapseString.substring(sepIndex + 1));
            }
        }

        @Override
        public CdxLine apply(CdxLine previousLine, CdxLine currentLine) {
            boolean hasCollapseField = currentLine.hasMutableField("collapseCount");
            if (previousLine == null) {
                if (hasCollapseField && currentLine.getMutableField("collapseCount") == null) {
                    currentLine.setMutableField("collapseCount", 1);
                }
                return currentLine;
            }

            CharBuffer prevValue = previousLine.get(fieldName);
            CharBuffer currValue = currentLine.get(fieldName);

            if (hasCollapseField) {
                int prevCount;
                if (isCollapsing) {
                    prevCount = (int) previousLine.getMutableField("collapseCount");
                } else {
                    prevCount = 0;
                }

                int curCount = 1;
                if (currentLine.getMutableField("collapseCount") != null) {
                    curCount = (int) currentLine.getMutableField("collapseCount");
                }
                currentLine.setMutableField("collapseCount", prevCount + curCount);
            }

            if ((substrLength > 0) && (substrLength <= currValue.length())) {
                prevValue = prevValue.subSequence(0, substrLength);
                currValue = currValue.subSequence(0, substrLength);
            }

            if (!currValue.equals(prevValue)) {
                isCollapsing = false;
                return currentLine;
            }

            isCollapsing = true;
            return null;
        }

    }
}
