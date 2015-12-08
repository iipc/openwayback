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
import org.netpreserve.openwayback.cdxlib.FieldType;

/**
 * A filter restricting the date range for a result.
 */
public class FromToFilter implements Filter {

    final CharBuffer from;

    final CharBuffer to;

    /**
     * Constructs a new date range filter.
     * <p>
     * @param from the earliest date or null if starting from the beginning.
     * @param to the last date or null if getting to the end.
     */
    public FromToFilter(final CharSequence from, final CharSequence to) {
        this.from = CharBuffer.wrap(from);
        this.to = CharBuffer.wrap(to);
    }

    @Override
    public boolean include(CdxLine line) {
        CharBuffer dateField = line.get(FieldType.b);
        return dateField.compareTo(from) >= 0 && dateField.compareTo(to) <= 0;
    }

}
