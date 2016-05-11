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
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class BlockCdxSourceLineCounter extends BlockCdxSourceIterator {

    public BlockCdxSourceLineCounter(SourceDescriptor sourceDescriptor,
            Iterator<SourceBlock> blockIterator, String startKey, String endKey) {
        super(sourceDescriptor, blockIterator, startKey, endKey);
    }

    @Override
    BlockCdxSourceLineCounter init() {
        cdxBuffer = new CdxBuffer(sourceDescriptor.getInputFormat(), startFilter, endFilter);
        return this;
    }

    public long count() {
        try {
            long count = 0L;

            if (startFilter != null && blockIterator.hasNext()) {
                cdxBuffer.setByteBuf(fillBuffer(blockIterator.next(), cdxBuffer.getByteBuf())
                        .get());

                cdxBuffer.skipLines();
                if (endFilter != null && !blockIterator.hasNext()) {
                    count += cdxBuffer.countLinesCheckingFilter();
                } else {
                    count += cdxBuffer.countLines();
                }
            }

            while (blockIterator.hasNext()) {
                SourceBlock nextBlock = blockIterator.next();
                if (nextBlock.getLineCount() < 0
                        || (endFilter != null && !blockIterator.hasNext())) {

                    cdxBuffer.setByteBuf(fillBuffer(nextBlock, cdxBuffer.getByteBuf()).get());
                    if (endFilter != null && !blockIterator.hasNext()) {
                        count += cdxBuffer.countLinesCheckingFilter();
                    } else {
                        count += cdxBuffer.countLines();
                    }
                } else {
                    count += nextBlock.getLineCount();
                }
            }
            return count;
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

}
