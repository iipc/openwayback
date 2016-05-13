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
import java.io.StringWriter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class StringValueTest {

    /**
     * Test of toString method, of class StringValue.
     */
    @Test
    public void testToString() {
        StringBuilder str = new StringBuilder("Ab\tcd\"e\ud801\udc37a\\b/\b\f\n\r");
        StringValue value = StringValue.valueOf(str);

        assertThat(value).hasToString("Ab\tcd\"e\ud801\udc37a\\b/\b\f\n\r");
    }

    /**
     * Test of toJson method, of class StringValue.
     */
    @Test
    public void testToJson() throws IOException {
        StringBuilder str = new StringBuilder("Ab\tcd\"e\ud801\udc37a\\b/\b\f\n\r");
        StringValue value = StringValue.valueOf(str);

        StringWriter sw = new StringWriter();
        value.toJson(sw);

        assertThat(sw).hasToString("\"Ab\\tcd\\\"e\ud801\udc37a\\\\b/\\b\\f\\n\\r\"");
    }

}