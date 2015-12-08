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

import java.util.WeakHashMap;

import org.netpreserve.openwayback.cdxlib.SearchResult;

/**
 * Base implementation of {@link SearchResult}.
 * <p>
 * An instance of this class keeps a weak reference to all iterators created from it. If an instance
 * of this class is closed, all of its iterators not already garbage collected will also be closed.
 */
public abstract class AbstractSearchResult implements SearchResult {

    WeakHashMap<CdxIterator, Object> iterators = new WeakHashMap<>();

    @Override
    public final CdxIterator iterator() {
        if (iterators == null) {
            throw new IllegalStateException("Search result is closed");
        }

        CdxIterator iter = newIterator();
        iterators.put(iter, null);
        return iter;
    }

    @Override
    public void close() {
        if (iterators != null) {
            for (CdxIterator iter : iterators.keySet()) {
                iter.close();
            }
            iterators = null;
        }
    }

    /**
     * Subclasses should implement this method instead of {@link #iterator()}.
     * <p>
     * @return the newly created {@link CdxIterator}
     */
    protected abstract CdxIterator newIterator();

}
