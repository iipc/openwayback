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

/**
 *
 */
public class UnparsedObjectValue implements Value {
    private final String value;

    private UnparsedObjectValue(String value) {
        this.value = value;
    }

    public static UnparsedObjectValue valueOf(char[] src, int start, int end) {
        return new UnparsedObjectValue(String.copyValueOf(src, start, end - start));
    }

    @Override
    public String toString() {
        return value;
    }

}
