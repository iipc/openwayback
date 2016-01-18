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
package org.netpreserve.openwayback.cdxlib.cdxsource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;
import org.netpreserve.openwayback.cdxlib.functions.Filter;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.processor.Processor;
import org.netpreserve.openwayback.cdxlib.CdxSource;
import org.netpreserve.openwayback.cdxlib.functions.FieldRegexFilter;
import org.netpreserve.openwayback.cdxlib.SearchResult;
import org.netpreserve.openwayback.cdxlib.processor.FilterProcessor;

import static org.assertj.core.api.Assertions.*;

/**
 * Test block based CDX source using file backend for these tests.
 */
public class BlockCdxSourceTest {

    private final CdxLineSchema outputFormat = new CdxLineSchema(' ', "urlkey", "timestamp");

    private final CdxLineFormatMapper mapper = new CdxLineFormatMapper(outputFormat, null);

    private final Comparator<CdxLine> comparator = new CdxLineComparator();

    /**
     * Test of search method, of class CdxFile.
     * <p>
     * Test with no filters.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testSearch() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {

            String startKey = null;
            String toKey = null;
            int expectedSize = 1666;

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);

            SearchResult result = cdxSource.search(startKey, toKey, outputFormat, null, false);
            assertThat(result).hasSize(expectedSize).usingElementComparator(comparator)
                    .startsWith(new CdxLine("as,hotel)/robots.txt 20070821143246", mapper))
                    .endsWith(new CdxLine("com,imrworldwide,server-no)/cgi-bin/count?http://www.nrk"
                                    + ".no/kanal/nrk_mpetre/1386661.html 20070825090945", mapper));

            // Collect result and reverse the order for the reversed test
            List<CdxLine> resultSet = new ArrayList<>();
            for (CdxLine l : result) {
                resultSet.add(l);
            }
            Collections.reverse(resultSet);

            // Check that revrse listing gives same number of records
            result = cdxSource.search(startKey, toKey, outputFormat, null, true);
            assertThat(result).hasSize(expectedSize).containsExactlyElementsOf(resultSet);
        }
    }

    /**
     * Test of search method, of class CdxFile.
     * <p>
     * Test with no filters.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testSearchNoArray() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        try (SourceDescriptor sourceDescriptor = new ReadOnlyBufferSourceDescriptor(path);) {

            String startKey = null;
            String toKey = null;
            int expectedSize = 1666;

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);

            SearchResult result = cdxSource.search(startKey, toKey, outputFormat, null, false);
            assertThat(result).hasSize(expectedSize).usingElementComparator(comparator)
                    .startsWith(new CdxLine("as,hotel)/robots.txt 20070821143246", mapper))
                    .endsWith(new CdxLine("com,imrworldwide,server-no)/cgi-bin/count?http://www.nrk"
                                    + ".no/kanal/nrk_mpetre/1386661.html 20070825090945", mapper));

            // Collect result and reverse the order for the reversed test
            List<CdxLine> resultSet = new ArrayList<>();
            for (CdxLine l : result) {
                resultSet.add(l);
            }
            Collections.reverse(resultSet);

            // Check that revrse listing gives same records in oposite order
            result = cdxSource.search(startKey, toKey, outputFormat, null, true);
            assertThat(result).hasSize(expectedSize)
                    .containsExactlyElementsOf(resultSet);

        }
    }

    /**
     * Test of search method, of class CdxFile.
     * <p>
     * Test with from/to filters.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testSearchWithFilter() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);

        try (CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);) {

            String startKey = "be,halten)";
            String toKey = "ch,";

            List<CdxLine> expectedResult = new ArrayList<>();
            expectedResult.add(new CdxLine("be,halten) 20070821143342", mapper));
            expectedResult.add(new CdxLine("be,your-counter)/robots.txt 20070821133445", mapper));
            expectedResult.add(new CdxLine("biz,dataparty-mn,www) 20070821170230", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/ 20070821185057", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/images/1-2.png 20070823215132", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/images/2-1.png 20070823215138", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/images/3-2.png 20070823215152", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/images/4-1.png 20070823215148", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/images/left_blue.jpg 20070823215146", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/images/top_blue.jpg 20070823215136", mapper));
            expectedResult
                    .add(new CdxLine("biz,ggs)/images/white_bullet.gif 20070823215134", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/images/white_tab.gif 20070823215150", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/menu.js 20070823215145", mapper));
            expectedResult.add(new CdxLine("biz,ggs)/new.css 20070823215140", mapper));
            expectedResult.add(new CdxLine("biz,ggs,www) 20070823215128", mapper));

            SearchResult result = cdxSource.search(startKey, toKey, outputFormat, null, false);
            assertThat(result).hasSize(15)
                    .usingElementComparator(comparator)
                    .containsExactlyElementsOf(expectedResult);

            // Check that revrse listing gives same records
            Collections.reverse(expectedResult);
            result = cdxSource.search(startKey, toKey, outputFormat, null, true);
            assertThat(result).hasSize(15)
                    .usingElementComparator(comparator)
                    .containsExactlyElementsOf(expectedResult);
        }
    }

    /**
     * Test of search method, of class CdxFile.
     * <p>
     * Test with content filter.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testSearchWithContentFilter() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);

        try (CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);) {

            String startKey = "be,halten)";
            String toKey = "ch,";

            FieldRegexFilter f = new FieldRegexFilter(Collections.singletonList("!statuscode:200"));
            Processor<Filter> fp = new FilterProcessor().addFunction(f);
            List filters = Collections.singletonList(fp);

            CdxLine expected = new CdxLine("be,your-counter)/robots.txt 20070821133445", mapper);
            SearchResult result = cdxSource.search(startKey, toKey, outputFormat, filters, false);
            assertThat(result).hasSize(1)
                    .usingElementComparator(comparator).containsExactly(expected);

            // Check that revrse listing gives same number of records
            result = cdxSource.search(startKey, toKey, outputFormat, filters, true);
            assertThat(result).hasSize(1)
                    .usingElementComparator(comparator).containsExactly(expected);
        }
    }

    /**
     * Comparator for CdxLines using compareTo instead of equals.
     * <p>
     * This is needed in these tests due to CdxLine not strictly following the equals() beeing
     * consistent with compareTo(). For an explenation see {@link CdxLine#compareTo(CdxLine).
     */
    private static class CdxLineComparator implements Comparator<CdxLine> {

        @Override
        public int compare(CdxLine o1, CdxLine o2) {
            return o1.compareTo(o2);
        }

    }

    /**
     * Wrapped CdxFileDescriptor which returns a read only ByteBuffe for the
     * {@link #read(SourceBlock, ByteBuffer)} method.
     * <p>
     * This class makes it possible to test that the BlockCdxSource works for ByteBuffers where
     * {@link ByteBuffer#hasArray()} returns false.
     */
    private static class ReadOnlyBufferSourceDescriptor extends CdxFileDescriptor {

        /**
         * Constructor delegating to CdxFileDescriptors constructor.
         * <p>
         * @param path path to cdx file
         * @throws IOException is thrown if something went wrong while reading the file
         */
        public ReadOnlyBufferSourceDescriptor(Path path) throws IOException {
            super(path);
        }

        @Override
        public ByteBuffer read(SourceBlock block, ByteBuffer byteBuf) throws IOException {
            return super.read(block, byteBuf).asReadOnlyBuffer();
        }

    }
}
