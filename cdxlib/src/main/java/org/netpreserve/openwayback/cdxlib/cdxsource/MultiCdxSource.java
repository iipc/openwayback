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
    public SearchResult search(String startKey, String endKey, CdxLineSchema outputFormat,
            List<Processor> processors, boolean reverse) {
        SearchResult[] sourceIterables = new SearchResult[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            sourceIterables[i] = sources.get(i)
                    .search(startKey, endKey, outputFormat, processors, reverse);
        }
        return new MultiCdxIterable(sourceIterables, processors, reverse);
    }

    @Override
    public long count(String startKey, String endKey) {
        long count = 0L;
        for (int i = 0; i < sources.size(); i++) {
            count += sources.get(i).count(startKey, endKey);
        }
        return count;
    }

    @Override
    public void close() throws IOException {
        for (CdxSource source : sources) {
            source.close();
        }
    }
    private class MultiCdxIterable extends AbstractSearchResult {

        SearchResult[] sourceIterables;

        List<Processor> processors;

        boolean reverse;

        public MultiCdxIterable(SearchResult[] sourceIterables, List<Processor> processors,
                boolean reverse) {
            this.sourceIterables = sourceIterables;
            this.processors = processors;
            this.reverse = reverse;
        }

        @Override
        public CdxIterator newIterator() {
            CdxIterator[] sourceIterators = new CdxIterator[sourceIterables.length];
            for (int i = 0; i < sourceIterables.length; i++) {
                sourceIterators[i] = sourceIterables[i].iterator();
            }
            CdxIterator iter = new MultiCdxIterator(parallel, reverse, sourceIterators);

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
