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
package org.netpreserve.openwayback.cdxlib.json;

import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;

/**
 *
 */
public class StringValue implements Value {
    private static final BitSet ILLEGAL_IN_JSON_STRING = new BitSet(256);

    static {
        ILLEGAL_IN_JSON_STRING.set('\"');
        ILLEGAL_IN_JSON_STRING.set('\\');
        ILLEGAL_IN_JSON_STRING.set('\b');
        ILLEGAL_IN_JSON_STRING.set('\f');
        ILLEGAL_IN_JSON_STRING.set('\n');
        ILLEGAL_IN_JSON_STRING.set('\r');
        ILLEGAL_IN_JSON_STRING.set('\t');
    }

    private final String value;

    private StringValue(String value) {
        this.value = value;
    }

    public static StringValue valueOf(char[] src, int start, int end) {
        return new StringValue(String.copyValueOf(src, start, end - start));
    }

    public static StringValue valueOf(StringBuilder sb) {
        return new StringValue(sb.toString());
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public void toJson(Writer out) throws IOException {
        out.write('\"');
        char[] src = value.toCharArray();
        for (char c : src) {
            if (ILLEGAL_IN_JSON_STRING.get(c)) {
                out.write('\\');
                switch (c) {
                    case '\b':
                        out.write('b');
                        break;
                    case '\f':
                        out.write('f');
                        break;
                    case '\n':
                        out.write('n');
                        break;
                    case '\r':
                        out.write('r');
                        break;
                    case '\t':
                        out.write('t');
                        break;
                    default:
                        out.write(c);
                }
            } else {
                out.write(c);
            }
        }
        out.write('\"');
    }

}
