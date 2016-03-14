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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.processor.Processor;
import org.netpreserve.openwayback.cdxlib.CdxSource;
import org.netpreserve.openwayback.cdxlib.SearchResult;

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
            final List<SourceBlock> blocks = sourceDescriptor.calculateBlocks(startUrl, endUrl);

            @Override
            public CdxIterator newIterator() {
                CdxIterator iterator;

                if (reverse) {
                    iterator = new BlockCdxSourceReverseIterator(sourceDescriptor,
                            reverseIterator(blocks),
                            startUrl, endUrl, lineFormatMapper).init();
                } else {
                    iterator = new BlockCdxSourceIterator(sourceDescriptor, blocks.iterator(),
                            startUrl, endUrl, lineFormatMapper).init();
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

    private static <E> Iterator<E> reverseIterator(final List<E> list) {
        return new Iterator<E>() {
            ListIterator<E> src = list.listIterator(list.size());

            @Override
            public boolean hasNext() {
                return src.hasPrevious();
            }

            @Override
            public E next() {
                return src.previous();
            }
        };
    }

    @Override
    public long count(final String startUrl, final String endUrl) {
        final CdxLineFormatMapper lineFormatMapper = new CdxLineFormatMapper(
                sourceDescriptor.getInputFormat(), null);

        final List<SourceBlock> blocks = sourceDescriptor.calculateBlocks(startUrl, endUrl);

        BlockCdxSourceLineCounter counter = new BlockCdxSourceLineCounter(sourceDescriptor,
                blocks.iterator(), startUrl, endUrl, lineFormatMapper).init();

        return counter.count();
    }

}
