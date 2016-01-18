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
import java.util.List;

import org.netpreserve.openwayback.cdxlib.SearchResult;
import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.processor.Processor;
import org.netpreserve.openwayback.cdxlib.CdxSource;

/**
 *
 */
public class BlockCdxSource implements CdxSource {

    final SourceDescriptor sourceDescriptor;

    public BlockCdxSource(SourceDescriptor sourceDescriptor) throws IOException {
        this.sourceDescriptor = sourceDescriptor;
    }

    @Override
    public SearchResult search(final String startUrl, final String endUrl,
            final CdxLineSchema outputFormat, final List<Processor> processors,
            final boolean reverse) {

        final CdxLineFormatMapper lineFormatMapper = new CdxLineFormatMapper(
                sourceDescriptor.getInputFormat(), outputFormat);

        return new AbstractSearchResult() {
            @Override
            public CdxIterator newIterator() {
                CdxIterator iterator;

                if (reverse) {
                    iterator = new BlockCdxSourceReverseIterator(sourceDescriptor, startUrl, endUrl,
                            lineFormatMapper);
                } else {
                    iterator = new BlockCdxSourceIterator(sourceDescriptor, startUrl, endUrl,
                            lineFormatMapper);
                }

                if (processors != null) {
                    for (Processor processorProvider : processors) {
                        iterator = processorProvider.processorIterator(iterator);
                    }
                }

                return iterator;
            }

        };
    }

    @Override
    public void close() {
        try {
            sourceDescriptor.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
