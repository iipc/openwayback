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
package org.netpreserve.openwayback.cdxlib.cdxsource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.junit.Test;
import org.netpreserve.openwayback.cdxlib.BaseCdxRecord;
import org.netpreserve.openwayback.cdxlib.CdxFormat;
import org.netpreserve.openwayback.cdxlib.CdxLineFormat;
import org.netpreserve.openwayback.cdxlib.CdxRecord;
import org.netpreserve.openwayback.cdxlib.CdxSource;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class ClosestCdxIteratorTest {

    private final CdxFormat format = CdxLineFormat.CDX11LINE;

    private final Comparator<CdxRecord> comparator = new CdxLineComparator();

    /**
     * Test of next method, of class ClosestCdxIterator.
     */
    @Test
    public void testNext() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile3.cdx").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {

            String url = "no,vg)/din_verden/assets/images/himmel.gif";
            String missingUrl = "no,vg)/din_verden/assets/images/hinder.gif";

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);
            CdxIterator it;

            // Test with url not in cdx
            it = new ClosestCdxIterator(cdxSource, missingUrl, "20070905173550", null);
            assertThat(it).isEmpty();

            // Test with timestamp equal to one of the lines
            it = new ClosestCdxIterator(cdxSource, url, "20070905173550", null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    BaseCdxRecord.create(url + " 20070905173550", format),
                    BaseCdxRecord.create(url + " 20070822103939", format));

            // Test with timestamp in between the lines
            it = new ClosestCdxIterator(cdxSource, url, "20070823173549", null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    BaseCdxRecord.create(url + " 20070822103939", format),
                    BaseCdxRecord.create(url + " 20070905173550", format));

            // Test with timestamp earlier than all the lines
            it = new ClosestCdxIterator(cdxSource, url, "20060823173549", null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    BaseCdxRecord.create(url + " 20070822103939", format),
                    BaseCdxRecord.create(url + " 20070905173550", format));

            // Test with timestamp later than all the lines
            it = new ClosestCdxIterator(cdxSource, url, "20090823173549", null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    BaseCdxRecord.create(url + " 20070905173550", format),
                    BaseCdxRecord.create(url + " 20070822103939", format));
        }
    }

    /**
     * Test of peek method, of class ClosestCdxIterator.
     */
    @Test
    public void testPeek() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile3.cdx").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {

            String url = "no,vg)/din_verden/assets/images/himmel.gif";
            String missingUrl = "no,vg)/din_verden/assets/images/hinder.gif";

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);
            CdxIterator it;

            it = new ClosestCdxIterator(cdxSource, missingUrl, "20070905173550", null);
            assertThat(it.peek()).isNull();

            it = new ClosestCdxIterator(cdxSource, url, "20070905173550", null);
            assertThat(it.peek())
                    .isEqualByComparingTo(BaseCdxRecord.create(url + " 20070905173550", format));

            it = new ClosestCdxIterator(cdxSource, url, "20070823173549", null);
            assertThat(it.peek())
                    .isEqualByComparingTo(BaseCdxRecord.create(url + " 20070822103939", format));
        }
    }

    /**
     * Test of hasNext method, of class ClosestCdxIterator.
     */
    @Test
    public void testHasNext() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile3.cdx").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {

            String url = "no,vg)/din_verden/assets/images/himmel.gif";
            String missingUrl = "no,vg)/din_verden/assets/images/hinder.gif";

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);
            CdxIterator it;

            it = new ClosestCdxIterator(cdxSource, missingUrl, "20070905173550", null);
            assertThat(it.hasNext()).isFalse();

            it = new ClosestCdxIterator(cdxSource, url, "20070905173550", null);
            assertThat(it.hasNext()).isTrue();

            it = new ClosestCdxIterator(cdxSource, url, "20070823173549", null);
            assertThat(it.hasNext()).isTrue();
        }
    }

}
