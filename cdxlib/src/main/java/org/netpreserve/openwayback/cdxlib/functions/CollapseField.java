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
 *
 */
public class CollapseField implements BiFunction {

    private static final String FIELD_SEP_CHAR = ":";

    private final String fieldName;

    private final int substrLength;

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
        if (previousLine == null) {
            return currentLine;
        }

        CharBuffer prevValue = previousLine.get(fieldName);
        CharBuffer currValue = currentLine.get(fieldName);

        if ((substrLength > 0) && (substrLength <= currValue.length())) {
            prevValue = prevValue.subSequence(0, substrLength);
            currValue = currValue.subSequence(0, substrLength);
        }

        if (!currValue.equals(prevValue)) {
            return currentLine;
        }

        return null;
    }

}
