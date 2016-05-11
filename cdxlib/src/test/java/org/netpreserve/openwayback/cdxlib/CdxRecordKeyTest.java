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
public class CdxRecordKeyTest {

    /**
     * Test of getUriKey and getTimeStamp methods, of class CdxRecordKey.
     */
    @Test
    public void testGetUriKeyAndGetTimeStamp() {
        String uriKey = "abc";
        String timeStamp = "123";
        char[] recordKey = (uriKey + " " + timeStamp).toCharArray();

        CdxRecordKey instance = new CdxRecordKey(recordKey);
        assertThat(instance.getUriKey()).isEqualTo(uriKey);
        assertThat(instance.getTimeStamp()).isEqualTo(timeStamp);

        instance = new CdxRecordKey(uriKey, timeStamp);
        assertThat(instance.getUriKey()).isEqualTo(uriKey);
        assertThat(instance.getTimeStamp()).isEqualTo(timeStamp);
    }

    /**
     * Test of compareTo method, of class CdxRecordKey.
     */
    @Test
    public void testCompareTo() {
        String uriKey1 = "abc";
        String timeStamp1 = "123";
        char[] recordKey1 = (uriKey1 + " " + timeStamp1).toCharArray();

        // Check that two records created from same source are equal
        CdxRecordKey instance1 = new CdxRecordKey(recordKey1);
        CdxRecordKey instance2 = new CdxRecordKey(recordKey1);
        assertThat(instance1).isEqualByComparingTo(instance2);

        instance1 = new CdxRecordKey(uriKey1, timeStamp1);
        instance2 = new CdxRecordKey(uriKey1, timeStamp1);
        assertThat(instance1).isEqualByComparingTo(instance2);

        // Check that two records created from different sources are equal
        instance1 = new CdxRecordKey(recordKey1);
        instance2 = new CdxRecordKey(uriKey1, timeStamp1);
        assertThat(instance1).isEqualByComparingTo(instance2);

        // Check less and greater than
        char[] recordKey2 = "abcd 123".toCharArray();
        char[] recordKey3 = "abc 124".toCharArray();

        instance1 = new CdxRecordKey(recordKey1);
        instance2 = new CdxRecordKey(recordKey2);
        CdxRecordKey instance3 = new CdxRecordKey(recordKey3);

        assertThat(instance1).isLessThan(instance2);
        assertThat(instance1).isLessThan(instance3);
        assertThat(instance3).isLessThan(instance2);

        assertThat(instance2).isGreaterThan(instance1);
        assertThat(instance3).isGreaterThan(instance1);
        assertThat(instance2).isGreaterThan(instance3);
    }

    /**
     * Test of toString method, of class CdxRecordKey.
     */
    @Test
    public void testToString() {
        String uriKey = "abc";
        String timeStamp = "123";
        char[] recordKey = (uriKey + " " + timeStamp).toCharArray();

        CdxRecordKey instance = new CdxRecordKey(recordKey);
        assertThat(instance).hasToString(new String(recordKey));

        instance = new CdxRecordKey(uriKey, timeStamp);
        assertThat(instance).hasToString(new String(recordKey));
    }

}