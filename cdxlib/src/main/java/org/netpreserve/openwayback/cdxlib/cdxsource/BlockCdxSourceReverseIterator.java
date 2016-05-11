/*
 * Copyright 2016 IIPC.
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

/**
 * Iterator for searching backwards in cdx.
 */
public class BlockCdxSourceReverseIterator extends BlockCdxSourceIterator {

    /**
     * Construct a new BlockCdxSourceReverseIterator.
     * <p>
     * @param sourceDescriptor the source descriptor to use
     * @param blockIterator an iterator over blocks which contain data within the requested range
     * @param startKey the range start (inclusive)
     * @param endKey the range end (exclusive)
     */
    public BlockCdxSourceReverseIterator(final SourceDescriptor sourceDescriptor,
            final Iterator<SourceBlock> blockIterator, final String startKey, final String endKey) {
        super(sourceDescriptor, blockIterator, startKey, endKey);
    }

    @Override
    BlockCdxSourceIterator init() {
        cdxBuffer = new ReverseCdxBuffer(sourceDescriptor.getInputFormat(), startFilter, endFilter);
        fillBuffer();
        skipLines();
        return this;
    }

}
