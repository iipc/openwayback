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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.functions.CollapseFieldProvider.CollapseField;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class CollapseFieldProviderTest {

    CdxLineSchema inFormat = new CdxLineSchema(' ', "urlkey", "timestamp");

    CdxLineFormatMapper lineFormat = new CdxLineFormatMapper(inFormat, null);

    /**
     * Test of apply method, of class CollapseField.
     */
    @Test
    public void testApplySubpartOfField() {
        // test with 4 digit timestamp
        CdxLine line1 = new CdxLine("no,dagbladet)/spiller_2519.html 20070908002541", lineFormat);
        CdxLine line2 = new CdxLine("no,dagbladet)/spiller_2520.html 20070908002532", lineFormat);
        CdxLine line3 = new CdxLine("no,dagbladet)/spiller_2520.html 20080908002532", lineFormat);
        CollapseField cf = new CollapseFieldProvider(Collections.singletonList("timestamp:4"))
                .newFunction();

        assertThat(cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat(cf.apply(line1, line2)).isNull();
        assertThat(cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");

        // test with 12 digit timestamp
        line1 = new CdxLine("no,dagbladet)/spiller_2519.html 20070908002541", lineFormat);
        line2 = new CdxLine("no,dagbladet)/spiller_2520.html 20070908002532", lineFormat);
        line3 = new CdxLine("no,dagbladet)/spiller_2520.html 20080908002532", lineFormat);
        cf = new CollapseFieldProvider(Collections.singletonList("timestamp:12")).newFunction();

        assertThat(cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat(cf.apply(line1, line2)).isNull();
        assertThat(cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");

        // test with 13 digit timestamp
        line1 = new CdxLine("no,dagbladet)/spiller_2519.html 20070908002541", lineFormat);
        line2 = new CdxLine("no,dagbladet)/spiller_2520.html 20070908002532", lineFormat);
        line3 = new CdxLine("no,dagbladet)/spiller_2520.html 20080908002532", lineFormat);
        cf = new CollapseFieldProvider(Collections.singletonList("timestamp:13")).newFunction();

        assertThat(cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat(cf.apply(line1, line2)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20070908002532");
        assertThat(cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");

        // test with 14 digit timestamp
        line1 = new CdxLine("no,dagbladet)/spiller_2519.html 20070908002541", lineFormat);
        line2 = new CdxLine("no,dagbladet)/spiller_2520.html 20070908002532", lineFormat);
        line3 = new CdxLine("no,dagbladet)/spiller_2520.html 20080908002532", lineFormat);
        cf = new CollapseFieldProvider(Collections.singletonList("timestamp:14")).newFunction();

        assertThat(cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat(cf.apply(line1, line2)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20070908002532");
        assertThat(cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");

        // test with 18 digit timestamp (larger than timestamp field)
        line1 = new CdxLine("no,dagbladet)/spiller_2519.html 20070908002541", lineFormat);
        line2 = new CdxLine("no,dagbladet)/spiller_2520.html 20070908002532", lineFormat);
        line3 = new CdxLine("no,dagbladet)/spiller_2520.html 20080908002532", lineFormat);
        cf = new CollapseFieldProvider(Collections.singletonList("timestamp:18")).newFunction();

        assertThat(cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat(cf.apply(line1, line2)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20070908002532");
        assertThat(cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");
    }

    @Test
    public void testApplyTwoFiles() {
        CdxLine line11 = new CdxLine("no,dagbladet)/spiller_2519.html 20070908002541", lineFormat);
        CdxLine line12 = new CdxLine("no,dagbladet)/spiller_2520.html 20070908002532", lineFormat);
        CdxLine line13 = new CdxLine("no,dagbladet)/spiller_2521.html 20080908002533", lineFormat);

        CdxLine line21 = new CdxLine("no,dagbladet)/spiller_2519.html 20070908002540", lineFormat);
        CdxLine line22 = new CdxLine("no,dagbladet)/spiller_2520.html 20070908002533", lineFormat);
        CdxLine line23 = new CdxLine("no,dagbladet)/spiller_2521.html 20080908002534", lineFormat);
        CdxLine line24 = new CdxLine("no,dagbladet)/spiller_2522.html 20090908002534", lineFormat);

        CollapseFieldProvider cfp = new CollapseFieldProvider(Collections
                .singletonList("timestamp:4"));

        // Simulate run of cdx source 1
        CollapseField cf = cfp.newFunction();
        assertThat(cf.apply(null, line11)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat(cf.apply(line11, line12)).isNull();
        assertThat(cf.apply(line12, line13)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2521.html 20080908002533");

        // Simulate run of cdx source 2
        cf = cfp.newFunction();
        assertThat(cf.apply(null, line21)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002540");
        assertThat(cf.apply(line21, line22)).isNull();
        assertThat(cf.apply(line22, line23)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2521.html 20080908002534");
        assertThat(cf.apply(line23, line24)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2522.html 20090908002534");

        // Simulate run of multi cdx source results from 1 and 2
        cf = cfp.newFunction();
        assertThat(cf.apply(null, line21)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002540");
        assertThat(cf.apply(line21, line11)).isNull();
        assertThat(cf.apply(line11, line13)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2521.html 20080908002533");
        assertThat(cf.apply(line13, line23)).isNull();
        assertThat(cf.apply(line23, line24))
                .hasToString("no,dagbladet)/spiller_2522.html 20090908002534");
    }

    @Test
    public void testApplyUrlkey() {
        CdxLine line1 = new CdxLine("ab 00", lineFormat);
        CdxLine line2 = new CdxLine("ab 01", lineFormat);
        CdxLine line3 = new CdxLine("ac 00", lineFormat);
        CdxLine line4 = new CdxLine("ac 01", lineFormat);

        CollapseField cf = new CollapseFieldProvider(toCollapseList("urlkey")).newFunction();

        assertThat(cf.apply(null, line1)).isNotNull().isSameAs(line1).hasToString("ab 00");
        assertThat(cf.apply(line1, line2)).isNull();
        assertThat(cf.apply(line2, line3)).isNotNull().isSameAs(line3).hasToString("ac 00");
        assertThat(cf.apply(line3, line4)).isNull();
    }

    @Test
    public void testApplyTimestamp() {
        CdxLine line1 = new CdxLine("ab 00", lineFormat);
        CdxLine line2 = new CdxLine("ab 01", lineFormat);
        CdxLine line3 = new CdxLine("ac 00", lineFormat);
        CdxLine line4 = new CdxLine("ac 01", lineFormat);

        CollapseField cf = new CollapseFieldProvider(toCollapseList("timestamp")).newFunction();

        assertThat(cf.apply(null, line1)).isNotNull().isSameAs(line1).hasToString("ab 00");
        assertThat(cf.apply(line1, line2)).isNotNull().isSameAs(line2).hasToString("ab 01");
        assertThat(cf.apply(line2, line3)).isNull();
        assertThat(cf.apply(line3, line4)).isNull();
    }

    @Test
    public void testApplyUrlkeyAndTimestamp() {
        CdxLine line1 = new CdxLine("ab 00", lineFormat);
        CdxLine line2 = new CdxLine("ab 01", lineFormat);
        CdxLine line3 = new CdxLine("ac 00", lineFormat);
        CdxLine line4 = new CdxLine("ac 01", lineFormat);

        CollapseField cf = new CollapseFieldProvider(toCollapseList("urlkey,timestamp"))
                .newFunction();

        assertThat(cf.apply(null, line1)).isNotNull().isSameAs(line1).hasToString("ab 00");
        assertThat(cf.apply(line1, line2)).isNotNull().isSameAs(line2).hasToString("ab 01");
        assertThat(cf.apply(line2, line3)).isNotNull().isSameAs(line3).hasToString("ac 00");
        assertThat(cf.apply(line3, line4)).isNotNull().isSameAs(line4).hasToString("ac 01");
    }

    @Test
    public void testApplyTimestampAndUrlkey() {
        CdxLine line1 = new CdxLine("ab 00", lineFormat);
        CdxLine line2 = new CdxLine("ab 01", lineFormat);
        CdxLine line3 = new CdxLine("ac 00", lineFormat);
        CdxLine line4 = new CdxLine("ac 01", lineFormat);

        CollapseField cf = new CollapseFieldProvider(toCollapseList("timestamp,urlkey"))
                .newFunction();

        assertThat(cf.apply(null, line1)).isNotNull().isSameAs(line1).hasToString("ab 00");
        assertThat(cf.apply(line1, line2)).isNotNull().isSameAs(line2).hasToString("ab 01");
        assertThat(cf.apply(line2, line3)).isNotNull().isSameAs(line3).hasToString("ac 00");
        assertThat(cf.apply(line3, line4)).isNotNull().isSameAs(line4).hasToString("ac 01");
    }

    @Test
    public void testApplyUrlkeyAndTimestampSeparateCollapseFunction() {
        CdxLine line1 = new CdxLine("ab 00", lineFormat);
        CdxLine line2 = new CdxLine("ab 01", lineFormat);
        CdxLine line3 = new CdxLine("ac 00", lineFormat);
        CdxLine line4 = new CdxLine("ac 01", lineFormat);

        CollapseField cf1 = new CollapseFieldProvider(toCollapseList("urlkey"))
                .newFunction();
        CollapseField cf2 = new CollapseFieldProvider(toCollapseList("timestamp"))
                .newFunction();

        CdxLine res11 = cf1.apply(null, line1);
        assertThat(res11).isNotNull().isSameAs(line1).hasToString("ab 00");
        CdxLine res21 = cf2.apply(null, res11);
        assertThat(res21).isNotNull().isSameAs(line1).hasToString("ab 00");

        CdxLine res12 = cf1.apply(line1, line2);
        assertThat(res12).isNull();

        CdxLine res13 = cf1.apply(line2, line3);
        assertThat(res13).isNotNull().isSameAs(line3).hasToString("ac 00");
        CdxLine res22 = cf2.apply(res21, res13);
        assertThat(res22).isNull();

        CdxLine res14 = cf1.apply(line3, line4);
        assertThat(res14).isNull();
    }

    @Test
    public void testApplyTimestampAndUrlkeySeparateCollapseFunction() {
        CdxLine line1 = new CdxLine("ab 00", lineFormat);
        CdxLine line2 = new CdxLine("ab 01", lineFormat);
        CdxLine line3 = new CdxLine("ac 00", lineFormat);
        CdxLine line4 = new CdxLine("ac 01", lineFormat);

        CollapseField cf1 = new CollapseFieldProvider(toCollapseList("timestamp"))
                .newFunction();
        CollapseField cf2 = new CollapseFieldProvider(toCollapseList("urlkey"))
                .newFunction();

        CdxLine res11 = cf1.apply(null, line1);
        assertThat(res11).isNotNull().isSameAs(line1).hasToString("ab 00");
        CdxLine res21 = cf2.apply(null, res11);
        assertThat(res21).isNotNull().isSameAs(line1).hasToString("ab 00");

        CdxLine res12 = cf1.apply(line1, line2);
        assertThat(res12).isNotNull().isSameAs(line2).hasToString("ab 01");

        CdxLine res13 = cf1.apply(line2, line3);
        assertThat(res13).isNull();

        CdxLine res14 = cf1.apply(line3, line4);
        assertThat(res14).isNull();
    }

    private List<String> toCollapseList(String collapseString) {
        return Arrays.asList(collapseString.split(","));
    }

}
