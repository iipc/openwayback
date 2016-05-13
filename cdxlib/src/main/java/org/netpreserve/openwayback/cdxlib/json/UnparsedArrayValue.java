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

/**
 *
 */
public class UnparsedArrayValue implements Value {

    private final String value;

    private UnparsedArrayValue(String value) {
        this.value = value;
    }

    public static UnparsedArrayValue valueOf(char[] src, int start, int end) {
        return new UnparsedArrayValue(String.copyValueOf(src, start, end - start));
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public void toJson(Writer out) throws IOException {
        out.write(value);
    }

}
