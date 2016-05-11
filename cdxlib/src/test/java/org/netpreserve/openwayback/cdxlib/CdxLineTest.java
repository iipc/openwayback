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

package org.netpreserve.openwayback.cdxlib;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class CdxLineTest {

    /**
     * Test of get method, of class CdxLine.
     */
    @Test
    public void testGet() {
        String line = "as,terra)/gfx/whitepixel.gif 20070821183528"
                + " https://www.terra.as/gfx/whitepixel.gif image/gif 200"
                + " FQ2WDWANAVUTLNKHGLRCOG4HKQWKHLVQ - - 396 36942074"
                + " IAH-20070821182921-00150.arc.gz";
        CdxLine instance = new CdxLine(line, CdxLineSchema.CDX11LINE);

        System.out.println("'" + instance.get(FieldName.URI_KEY) + "'");
        System.out.println("'" + instance.get(FieldName.TIMESTAMP) + "'");
        System.out.println("'" + instance.get(FieldName.ORIGINAL_URI) + "'");
        System.out.println("'" + instance.get(FieldName.MIME_TYPE) + "'");
        System.out.println("'" + instance.get(FieldName.RESPONSE_CODE) + "'");
        System.out.println("'" + instance.get(FieldName.ROBOT_FLAGS) + "'");
        System.out.println("'" + instance.get(FieldName.REDIRECT) + "'");
        System.out.println("'" + instance.get(FieldName.LENGTH) + "'");
        System.out.println("'" + instance.get(FieldName.DIGEST) + "'");
        System.out.println("'" + instance.get(FieldName.OFFSET) + "'");
        System.out.println("'" + instance.get(FieldName.FILENAME) + "'");

        System.out.println("\n'" + instance.getKey() + "'");
        System.out.println("'" + instance.getKey().getUriKey() + "'");
        System.out.println("'" + instance.getKey().getTimeStamp() + "'");

//        fail("Prototype");
    }

    /**
     * Test of toString method, of class CdxLine.
     */
    @Test
    public void testToString() {
    }

    /**
     * Test of hashCode method, of class CdxLine.
     */
    @Test
    public void testHashCode() {
    }

    /**
     * Test of equals method, of class CdxLine.
     */
    @Test
    public void testEquals() {
    }

}