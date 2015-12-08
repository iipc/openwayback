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

import java.util.Iterator;

import org.netpreserve.openwayback.cdxlib.CdxLine;

/**
 * An {@link Iterator} over CDX lines from a CdxSource.
 * <p>
 * The CdxIterator differs from the general contract of {@link Iterator} in two ways:
 * <ul>
 * <li>CdxIterator is not allowed to have null values.</li>
 * <li>CdxIterator should never throw {@link java.util.NoSuchElementException}. Instead null is
 * returned to indicate end of iteration.</li>
 * </ul>
 * <p>
 * Even though this class implements a close method. Users of the class should normally not need to
 * close i t(but it is harmless to do so,) because the Iterable creating the iterator should
 * normally do it for you.
 */
public interface CdxIterator extends Iterator<CdxLine>, AutoCloseable {

    /**
     * Returns the next element in the iteration.
     * <p>
     * @return the next element in the iteration or null if the iteration has no more elements.
     */
    @Override
    CdxLine next();

    /**
     * Returns the next element in the iteration, without advancing the iteration.
     * <p>
     * <p>
     * Calls to {@code peek()} does not change the state of the iteration.
     * <p>
     * @return the next element in the iteration or null if there are no more elements.
     * {@link #hasNext()}
     */
    CdxLine peek();

    @Override
    void close();

}
