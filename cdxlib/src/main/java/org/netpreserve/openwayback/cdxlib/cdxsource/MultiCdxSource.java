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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.netpreserve.openwayback.cdxlib.SearchResult;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.processor.Processor;
import org.netpreserve.openwayback.cdxlib.CdxSource;
import org.netpreserve.openwayback.cdxlib.processor.BiFunctionProcessor;

/**
 * A {@link CdxSource} composed of multiple other sources.
 */
public class MultiCdxSource implements CdxSource {

    private boolean parallel = false;

    private final List<CdxSource> sources;

    /**
     * Creates an empty MultiCdxSource.
     */
    public MultiCdxSource() {
        this.sources = new ArrayList<>();
    }

    /**
     * Creates a MultiCdxSource from an array of {@link CdxSource}s.
     *
     * @param sources the array of sources to start out with
     */
    public MultiCdxSource(CdxSource... sources) {
        this.sources = Arrays.asList(sources);
    }

    /**
     * Creates a MultiCdxSource from a list of {@link CdxSource}s.
     *
     * @param sources the list of sources to start out with
     */
    public MultiCdxSource(List<CdxSource> sources) {
        this.sources = sources;
    }

    /**
     * Add a {@link CdxSource} to this MultiCdxSource.
     *
     * @param source the source to add
     */
    public void addSource(CdxSource source) {
        this.sources.add(source);
    }

    @Override
    public SearchResult search(String startUrl, String endUrl, CdxLineSchema outputFormat,
            List<Processor> processors) {
        CdxIterator[] sourceIterators = new CdxIterator[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            sourceIterators[i] = sources.get(i)
//                    .search(startUrl, endUrl, outputFormat, null).iterator();
                    .search(startUrl, endUrl, outputFormat, processors).iterator();
        }
        return new MultiCdxIterable(sourceIterators, processors);
    }

    @Override
    public void close() throws IOException {
        for (CdxSource source : sources) {
            source.close();
        }
    }

    private class MultiCdxIterable extends AbstractSearchResult {

        CdxIterator[] sourceIterators;

        List<Processor> processors;

        public MultiCdxIterable(CdxIterator[] sourceIterators, List<Processor> processors) {
            this.sourceIterators = sourceIterators;
            this.processors = processors;
        }

        @Override
        public CdxIterator newIterator() {
            CdxIterator iter = new MultiCdxIterator(parallel, sourceIterators);

            if (processors != null) {
                for (Processor p : processors) {
                    if (p instanceof BiFunctionProcessor) {
                        iter = p.processorIterator(iter);
                    }
                }
            }
            return iter;
        }

    }

}
