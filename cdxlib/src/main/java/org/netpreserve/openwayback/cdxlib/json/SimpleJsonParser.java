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

import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.Map;

import org.netpreserve.openwayback.cdxlib.FieldName;

/**
 *
 */
public class SimpleJsonParser {

    private static final BitSet LEGAL_END_OF_NUMBER = new BitSet(256);

    static {
        LEGAL_END_OF_NUMBER.set(' ');
        LEGAL_END_OF_NUMBER.set(',');
        LEGAL_END_OF_NUMBER.set('}');
        LEGAL_END_OF_NUMBER.set(']');
    }

    private static final BitSet LEGAL_START_OF_VALUE = new BitSet(256);

    static {
        LEGAL_START_OF_VALUE.set('\"');
        LEGAL_START_OF_VALUE.set('-');
        LEGAL_START_OF_VALUE.set('0');
        LEGAL_START_OF_VALUE.set('1');
        LEGAL_START_OF_VALUE.set('2');
        LEGAL_START_OF_VALUE.set('3');
        LEGAL_START_OF_VALUE.set('4');
        LEGAL_START_OF_VALUE.set('5');
        LEGAL_START_OF_VALUE.set('6');
        LEGAL_START_OF_VALUE.set('7');
        LEGAL_START_OF_VALUE.set('8');
        LEGAL_START_OF_VALUE.set('9');
        LEGAL_START_OF_VALUE.set('{');
        LEGAL_START_OF_VALUE.set('[');
        LEGAL_START_OF_VALUE.set('t');
        LEGAL_START_OF_VALUE.set('f');
        LEGAL_START_OF_VALUE.set('n');
    }

    private static final char[] TRUE_VALUE = "true".toCharArray();

    private static final char[] FALSE_VALUE = "false".toCharArray();

    private static final char[] NULL_VALUE = "null".toCharArray();

    private int currentIdx;

    private final char[] src;

    public SimpleJsonParser(final char[] src, final int offset) {
        this.currentIdx = offset;
        this.src = src;
    }

    public Map<FieldName, Value> parseObject() {
        skipSpace();

        if (src[currentIdx] != '{') {
            throw new IllegalArgumentException("Illegal start of JSON object");
        }
        currentIdx++;
        skipSpace();

        Map<FieldName, Value> fields = new IdentityHashMap<>();

        while (true) {
            if (src[currentIdx] == '}') {
                // Found end of object
                currentIdx++;
                return fields;
            } else if (src[currentIdx] == ',') {
                currentIdx++;
            }

            fields.put(parseFieldName(), parseValue());

            skipSpace();
        }
    }

    private void skipSpace() {
        while (src[currentIdx] == ' ') {
            currentIdx++;
        }
    }

    private FieldName parseFieldName() {
        skipSpace();
        FieldName name = FieldName.forName(parseString().toString());
        skipSpace();
        if (src[currentIdx] != ':') {
            System.out.println("'" + src[currentIdx] + "'");
            throw new IllegalArgumentException("Missing colon after field name");
        }
        currentIdx++;
        return name;
    }

    private Value parseValue() {
        skipSpace();
        if (!isInBitSet(src[currentIdx], LEGAL_START_OF_VALUE)) {
            System.out.println(src[currentIdx]);
            throw new IllegalArgumentException("Not a value");
        }
        switch (src[currentIdx]) {
            case '"':
                return parseString();
            case '[':
                return parseUnparsedArray();
            case '{':
                return parseUnparsedObject();
            case 't':
                return parseTrue();
            case 'f':
                return parseFalse();
            case 'n':
                return parseNull();
            default:
                return parseNumber();
        }
    }

    private StringValue parseString() {
        if (src[currentIdx] != '\"') {
            throw new IllegalArgumentException("Illegal start of JSON string");
        }
        currentIdx++;
        int start = currentIdx;
        StringBuilder sb = null;
        while (src[currentIdx] != '\"') {
            // Check for escaped characters
            if (src[currentIdx] == '\\') {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(src, start, currentIdx - start);

                currentIdx++;
                switch (src[currentIdx]) {
                    case '\"':
                        sb.append('\"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        int val = Integer.parseInt(String.copyValueOf(src, currentIdx + 1, 4), 16);

                        // Check if the value is the first of a UTF-16 surrogate pair.
                        if (Character.isHighSurrogate((char) val)
                                && src[currentIdx + 5] == '\\'
                                && src[currentIdx + 6] == 'u') {
                            int val2 = Integer.parseInt(
                                    String.copyValueOf(src, currentIdx + 7, 4), 16);

                            sb.appendCodePoint(Character.toCodePoint((char) val, (char) val2));
                            currentIdx += 10;
                        } else {
                            sb.append((char) val);
                            currentIdx += 4;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal escaped character in string");
                }
                start = currentIdx + 1;
            }
            currentIdx++;
        }

        if (sb != null) {
            sb.append(src, start, currentIdx++ - start);
            return StringValue.valueOf(sb);
        } else {
            return StringValue.valueOf(src, start, currentIdx++);
        }
    }

    private void skipString() {
        if (src[currentIdx] != '\"') {
            throw new IllegalArgumentException("Illegal start of JSON string");
        }
        currentIdx++;
        while (src[currentIdx] != '\"') {
            // Check for escaped characters
            if (src[currentIdx] == '\\') {
                currentIdx++;
                switch (src[currentIdx]) {
                    case '\"':
                    case '\\':
                    case '/':
                    case 'b':
                    case 'f':
                    case 'n':
                    case 'r':
                    case 't':
                    case 'u':
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal escaped character in string");
                }
            }
            currentIdx++;
        }
    }

    private UnparsedArrayValue parseUnparsedArray() {
        if (src[currentIdx] != '[') {
            throw new IllegalArgumentException("Illegal start of JSON array");
        }
        int nestingLevel = 1;
        int start = currentIdx++;
        while (nestingLevel > 0) {
            if (src[currentIdx] == ']') {
                nestingLevel--;
            } else if (src[currentIdx] == '[') {
                nestingLevel++;
            } else if (src[currentIdx] == '\"') {
                skipString();
            }
            currentIdx++;
        }
        return UnparsedArrayValue.valueOf(src, start, currentIdx);
    }

    private UnparsedObjectValue parseUnparsedObject() {
        if (src[currentIdx] != '{') {
            throw new IllegalArgumentException("Illegal start of JSON object");
        }
        int nestingLevel = 1;
        int start = currentIdx++;
        while (nestingLevel > 0) {
            if (src[currentIdx] == '}') {
                nestingLevel--;
            } else if (src[currentIdx] == '{') {
                nestingLevel++;
            } else if (src[currentIdx] == '\"') {
                skipString();
            }
            currentIdx++;
        }
        return UnparsedObjectValue.valueOf(src, start, currentIdx);
    }

    private NumberValue parseNumber() {
        int start = currentIdx;
        while (!isInBitSet(src[currentIdx], LEGAL_END_OF_NUMBER)) {
            currentIdx++;
        }
        return NumberValue.valueOf(src, start, currentIdx);
    }

    private BooleanValue parseTrue() {
        check(TRUE_VALUE);
        return BooleanValue.TRUE;
    }

    private BooleanValue parseFalse() {
        check(FALSE_VALUE);
        return BooleanValue.FALSE;
    }

    private NullValue parseNull() {
        check(NULL_VALUE);
        return NullValue.NULL;
    }

    private void check(char[] match) {
        if (currentIdx + match.length > src.length) {
            throw new IllegalArgumentException("Not a value");
        }
        for (int i = 0; i < match.length; i++) {
            if (src[currentIdx++] != match[i]) {
                throw new IllegalArgumentException("Not a value");
            }
        }
    }

    private boolean isInBitSet(char ch, BitSet bitSet) {
        return ch > 0 && ch < 256 && bitSet.get(ch);
    }
}
