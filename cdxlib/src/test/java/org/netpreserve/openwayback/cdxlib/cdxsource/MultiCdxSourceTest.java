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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.netpreserve.openwayback.cdxlib.functions.Filter;
import org.netpreserve.openwayback.cdxlib.CdxRecord;
import org.netpreserve.openwayback.cdxlib.processor.Processor;
import org.netpreserve.openwayback.cdxlib.CdxSource;
import org.netpreserve.openwayback.cdxlib.functions.FieldRegexFilter;
import org.netpreserve.openwayback.cdxlib.SearchResult;
import org.netpreserve.openwayback.cdxlib.processor.FilterProcessor;

import static org.assertj.core.api.Assertions.*;

/**
 * Test merging of more than one CDX source.
 */
public class MultiCdxSourceTest {

    /**
     * Test of search method, of class MultiCdxSource.
     * <p>
     * Test with no filters.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testSearchNoFilter() throws URISyntaxException, IOException {
        Path path1 = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        Path path2 = Paths.get(ClassLoader.getSystemResource("cdxfile2.cdx").toURI());

        SourceDescriptor sourceDescriptor1 = new CdxFileDescriptor(path1);
        SourceDescriptor sourceDescriptor2 = new CdxFileDescriptor(path2);

        CdxSource cdxFile1 = new BlockCdxSource(sourceDescriptor1);
        CdxSource cdxFile2 = new BlockCdxSource(sourceDescriptor2);

        SearchResult result1 = cdxFile1.search(null, null, null, false);
        assertThat(result1).hasSize(1666);

        SearchResult result2 = cdxFile2.search(null, null, null, false);
        assertThat(result2).hasSize(1888);

        MultiCdxSource cdxSource = new MultiCdxSource(cdxFile1, cdxFile2);

        SearchResult resultTotal = cdxSource.search(null, null, null, false);
        assertThat(resultTotal).hasSize(1666 + 1888);

        // Collect result and reverse the order for the reversed test
        List<CdxRecord> resultSet = new ArrayList<>();
        for (CdxRecord l : resultTotal) {
            resultSet.add(l);
        }
        Collections.reverse(resultSet);

        SearchResult resultTotalReverse = cdxSource.search(null, null, null, true);
        assertThat(resultTotalReverse).hasSize(1666 + 1888).containsExactlyElementsOf(resultSet);
    }

    /**
     * Test of search method, of class MultiCdxSource.
     * <p>
     * Test with from/to filters.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testSearchWithFilter() throws URISyntaxException, IOException {
        Path path1 = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        Path path2 = Paths.get(ClassLoader.getSystemResource("cdxfile2.cdx").toURI());

        SourceDescriptor sourceDescriptor1 = new CdxFileDescriptor(path1);
        SourceDescriptor sourceDescriptor2 = new CdxFileDescriptor(path2);

        CdxSource cdxFile1 = new BlockCdxSource(sourceDescriptor1);
        CdxSource cdxFile2 = new BlockCdxSource(sourceDescriptor2);

        String startKey = "be,halten)";
        String toKey = "ch,";

        SearchResult result1 = cdxFile1.search(startKey, toKey, null, false);
        assertThat(result1).hasSize(15);

        SearchResult result2 = cdxFile2.search(startKey, toKey, null, false);
        assertThat(result2).hasSize(37);

        MultiCdxSource cdxSource = new MultiCdxSource(cdxFile1, cdxFile2);

        SearchResult resultTotal = cdxSource.search(startKey, toKey, null, false);
        assertThat(resultTotal).hasSize(15 + 37);

        // Collect result and reverse the order for the reversed test
        List<CdxRecord> resultSet = new ArrayList<>();
        for (CdxRecord l : resultTotal) {
            resultSet.add(l);
        }
        Collections.reverse(resultSet);

        SearchResult resultTotalReverse = cdxSource.search(startKey, toKey, null, true);
        assertThat(resultTotalReverse).hasSize(15 + 37).containsExactlyElementsOf(resultSet);
    }

    /**
     * Test of search method, of class MultiCdxSource.
     * <p>
     * Test with content filters.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testSearchWithContentFilter() throws URISyntaxException, IOException {
        Path path1 = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        Path path2 = Paths.get(ClassLoader.getSystemResource("cdxfile2.cdx").toURI());

        SourceDescriptor sourceDescriptor1 = new CdxFileDescriptor(path1);
        SourceDescriptor sourceDescriptor2 = new CdxFileDescriptor(path2);

        CdxSource cdxFile1 = new BlockCdxSource(sourceDescriptor1);
        CdxSource cdxFile2 = new BlockCdxSource(sourceDescriptor2);

        String startKey = "be,halten)";
        String toKey = "ch,";

        FieldRegexFilter f = new FieldRegexFilter(Collections.singletonList("!statuscode:200"));
        Processor<Filter> fp = new FilterProcessor().addFunction(f);
        List filters = Collections.singletonList(fp);

        SearchResult result1 = cdxFile1.search(startKey, toKey, filters, false);
        assertThat(result1).hasSize(1);

        SearchResult result2 = cdxFile2.search(startKey, toKey, filters, false);
        assertThat(result2).hasSize(4);

        MultiCdxSource cdxSource = new MultiCdxSource(cdxFile1, cdxFile2);

        SearchResult resultTotal = cdxSource.search(startKey, toKey, filters, false);
        assertThat(resultTotal).hasSize(1 + 4);

        // Collect result and reverse the order for the reversed test
        List<CdxRecord> resultSet = new ArrayList<>();
        for (CdxRecord l : resultTotal) {
            resultSet.add(l);
        }
        Collections.reverse(resultSet);

        SearchResult resultTotalReverse = cdxSource.search(startKey, toKey, filters, true);
        assertThat(resultTotalReverse).hasSize(1 + 4).containsExactlyElementsOf(resultSet);
    }

    @Test
    public void testCount() throws URISyntaxException, IOException {
        Path path1 = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        Path path2 = Paths.get(ClassLoader.getSystemResource("cdxfile2.cdx").toURI());

        SourceDescriptor sourceDescriptor1 = new CdxFileDescriptor(path1);
        SourceDescriptor sourceDescriptor2 = new CdxFileDescriptor(path2);

        CdxSource cdxFile1 = new BlockCdxSource(sourceDescriptor1);
        CdxSource cdxFile2 = new BlockCdxSource(sourceDescriptor2);

        long result1 = cdxFile1.count(null, null);
        assertThat(result1).isEqualTo(1666);

        long result2 = cdxFile2.count(null, null);
        assertThat(result2).isEqualTo(1888);

        MultiCdxSource cdxSource = new MultiCdxSource(cdxFile1, cdxFile2);

        long resultTotal = cdxSource.count(null, null);
        assertThat(resultTotal).isEqualTo(1666 + 1888);
    }
}
