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
package org.netpreserve.openwayback.cdxlib;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.netpreserve.openwayback.cdxlib.cdxsource.SourceDescriptor;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class SourceDescriptorFactoryTest {

    /**
     * Test of getDescriptor method, of class SourceDescriptorFactory.
     * <p>
     * @throws java.net.URISyntaxException should not be thrown in this test.
     */
    @Test
    public void testGetDescriptor() throws URISyntaxException {
        SourceDescriptor sd = SourceDescriptorFactory.getDescriptor("nonexistent", null, null);
        assertThat(sd).isNull();

        sd = SourceDescriptorFactory.getDescriptor("cdxfile",
                new URI("src/test/resources/cdxfile1.cdx"), null);
        assertThat(sd).isNotNull();

        sd = SourceDescriptorFactory.getDescriptor("cdxfile",
                ClassLoader.getSystemResource("cdxfile1.cdx").toURI(), null);
        assertThat(sd).isNotNull();
    }

}
