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

package org.netpreserve.openwayback.cdxlib.processor;

import org.junit.Test;
import org.netpreserve.openwayback.cdxlib.cdxsource.CdxIterator;
import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.cdxsource.MockCdxIterator;
import org.netpreserve.openwayback.cdxlib.functions.BiFunction;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BiFunctionProcessor.
 */
public class BiFunctionProcessorTest {

    /**
     * Test of processorIterator method, of class BiFunctionProcessor.
     */
    @Test
    public void testProcessorIterator() {
        CdxLineFormatMapper lineFormat = new CdxLineFormatMapper(CdxLineSchema.CDX11LINE, null);
        CdxLine line1 = new CdxLine("no,dagbladet)/premier2000/spiller_2519.html 20070908002541 "
                + "http://www.dagbladet.no/premier2000/spiller_2519.html text/html 404 "
                + "4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7 - - 1506 68224437 "
                + "IAH-20070907235053-00459-heritrix2.nb.no.arc.gz", lineFormat);

        CdxLine line2 = new CdxLine("no,dagbladet)/premier2000/spiller_2520.html 20070908002532 "
                + "http://www.dagbladet.no/premier2000/spiller_2520.html text/html 200 "
                + "5RRATYEFXZV5V64KA6AP3KFDK7LGF7TT - - 4014 89051462 "
                + "IAH-20070907231717-00457-heritrix2.nb.no.arc.gz", lineFormat);

        BiFunctionProcessor fp = new BiFunctionProcessor();
        fp.addFunction(new BiFunction() {
            @Override
            public CdxLine apply(CdxLine previousLine, CdxLine currentLine) {
                if (previousLine == null || !currentLine.equals(previousLine)) {
                    return currentLine;
                } else {
                    return null;
                }
            }

        });

        MockCdxIterator iter = new MockCdxIterator();
        iter.add(line1).add(line2).add(line2).add(line2);

        CdxIterator processedIterator = fp.processorIterator(iter);

        assertThat(processedIterator).hasSize(2);
    }

}