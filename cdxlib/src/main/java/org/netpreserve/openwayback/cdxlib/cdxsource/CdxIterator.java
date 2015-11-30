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
 */
public interface CdxIterator extends Iterator<CdxLine> {

    /**
     * Returns the next element in the iteration, without advancing the iteration.
     * <p>
     * <p>
     * Calls to {@code peek()} does not change the state of the iteration.
     * <p>
     * @throws NoSuchElementException if the iteration has no more elements according to
     * {@link #hasNext()}
     */
    CdxLine peek();

}
