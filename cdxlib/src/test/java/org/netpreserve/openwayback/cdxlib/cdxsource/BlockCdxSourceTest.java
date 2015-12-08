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
import java.util.Collections;
import java.util.List;

import org.junit.Test;
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

    private final CdxLineSchema outputFormat = CdxLineSchema.CDX09LINE;

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
            SearchResult result = cdxSource.search(startKey, toKey, outputFormat, null);

            assertThat(result).hasSize(expectedSize);
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

        CdxSource cdxFile = new BlockCdxSource(sourceDescriptor);

        String startKey = "be,halten)";
        String toKey = "ch,";

        SearchResult result = cdxFile.search(startKey, toKey, outputFormat, null);

        assertThat(result).hasSize(15);
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

        CdxSource cdxFile = new BlockCdxSource(sourceDescriptor);

        String startKey = "be,halten)";
        String toKey = "ch,";

        FieldRegexFilter f = new FieldRegexFilter(Collections.singletonList("!statuscode:200"));
        Processor<Filter> fp = new FilterProcessor().addFunction(f);
        List filters = Collections.singletonList(fp);

        SearchResult result = cdxFile.search(startKey, toKey, outputFormat, filters);

        assertThat(result).hasSize(1);
    }

}
