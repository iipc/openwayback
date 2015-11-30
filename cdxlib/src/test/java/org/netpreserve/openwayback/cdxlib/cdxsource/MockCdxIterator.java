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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.netpreserve.openwayback.cdxlib.CdxLine;

/**
 * Mock implementation of CdxIterator for unit tests.
 */
public class MockCdxIterator implements CdxIterator {

    private final List<CdxLine> data = new ArrayList<>();

    private int nextPosition = 0;

    /**
     * Add a CDX line to the iterator. If this is done while iterating, results are undefined.
     * <p>
     * @param line the line to add.
     * @return returns this to allow chained method calls.
     */
    public MockCdxIterator add(CdxLine line) {
        data.add(line);
        return this;
    }

    /**
     * Reset the iterator to be able to iterate over the same data again.
     * <p>
     * @return returns this to allow chained method calls.
     */
    public MockCdxIterator reset() {
        nextPosition = 0;
        return this;
    }

    @Override
    public CdxLine peek() {
        if (hasNext()) {
            return data.get(nextPosition);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public boolean hasNext() {
        return nextPosition < data.size();
    }

    @Override
    public CdxLine next() {
        if (hasNext()) {
            return data.get(nextPosition++);
        } else {
            throw new NoSuchElementException();
        }
    }

}
