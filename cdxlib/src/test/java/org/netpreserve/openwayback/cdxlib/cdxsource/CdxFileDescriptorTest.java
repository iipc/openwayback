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
import java.util.List;

import org.junit.Test;
import org.netpreserve.openwayback.cdxlib.CdxLineFormat;
import org.netpreserve.openwayback.cdxlib.FieldName;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CdxFileDescriptor.
 * <p>
 * Uses testfiles in src/test/resources
 */
public class CdxFileDescriptorTest {

    /**
     * Test of calculateBlocks method, of class CdxFileDescriptor.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testCalculateBlocks() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());

        CdxFileDescriptor meta = new CdxFileDescriptor(path);

        SourceBlock expectedBlock1 = new SourceBlock("as,hotel)/robots.txt", 27, 434660);

        List<SourceBlock> blocks = meta.calculateBlocks("ac,", "biz,");

        assertThat(blocks)
                .hasSize(1)
                .containsExactly(expectedBlock1);

        // Test with small file
        path = Paths.get(ClassLoader.getSystemResource("cdxfile3.cdx").toURI());
        meta = new CdxFileDescriptor(path);
        blocks = meta.calculateBlocks(null, null);

        assertThat(blocks)
                .hasSize(1);
    }

    /**
     * Test of getInputFormat method, of class CdxFileDescriptor.
     * <p>
     * @throws java.net.URISyntaxException should not happen in this test
     * @throws java.io.IOException should not happen in this test
     */
    @Test
    public void testGetInputFormat() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());

        CdxFileDescriptor meta = new CdxFileDescriptor(path);

        CdxLineFormat schema = (CdxLineFormat) meta.getInputFormat();

        assertThat(schema).isNotNull();
        assertThat(schema.getDelimiter()).isEqualTo(' ');
        assertThat(schema.getLength()).isEqualTo(11);
        assertThat(schema.getField(0)).isEqualTo(FieldName.URI_KEY);
    }

}
