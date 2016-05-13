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

import java.util.Map;

import org.junit.Test;
import org.netpreserve.openwayback.cdxlib.FieldName;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class SimpleJsonParserTest {

    /**
     * Test of parseObject method, of class SimpleJsonParser.
     */
    @Test
    public void testParse() {
        String src = "{\"string\": \"value\", \"array\":[\"val1\",\"val2\"], \"number\":123, "
                + "\"double\":123.456,\"true\": true,\"false\":false,\"null\":null,\"object\":{}"
                + "\"specialString\":\"AB\\\\C\\tD\\\"E\\u0041F\\ud801\\udc37G\","
                + "\"nestedArray\":[123,[456,\"ab\\tc\"],3,\"{}}A\\\"B[]]\"],"
                + "\"nestedObject\":{\"123\":[456,\"ab\\tc\"],\"a\":3,\"b\":\"{}}A\\\"B[]]\"}}";

        SimpleJsonParser instance = new SimpleJsonParser(src.toCharArray(), 0);
        Map<FieldName, Value> res = instance.parseObject();

        assertThat(res.get(FieldName.forName("array"))).hasToString("[\"val1\",\"val2\"]");
        assertThat(res.get(FieldName.forName("false"))).hasToString("false");
        assertThat(res.get(FieldName.forName("true"))).hasToString("true");
        assertThat(res.get(FieldName.forName("null"))).hasToString("null");
        assertThat(res.get(FieldName.forName("string"))).hasToString("value");
        assertThat(res.get(FieldName.forName("number"))).hasToString("123");
        assertThat(res.get(FieldName.forName("double"))).hasToString("123.456");
        assertThat(res.get(FieldName.forName("object"))).hasToString("{}");
        assertThat(res.get(FieldName.forName("specialString")))
                .hasToString("AB\\C\tD\"EAF\ud801\udc37G");
        assertThat(res.get(FieldName.forName("nestedArray")))
                .hasToString("[123,[456,\"ab\\tc\"],3,\"{}}A\\\"B[]]\"]");
        assertThat(res.get(FieldName.forName("nestedObject")))
                .hasToString("{\"123\":[456,\"ab\\tc\"],\"a\":3,\"b\":\"{}}A\\\"B[]]\"}");

    }

}