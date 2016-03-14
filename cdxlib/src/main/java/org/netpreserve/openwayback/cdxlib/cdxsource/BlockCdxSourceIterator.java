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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;

/**
 * A {@link CdxIterator} which iterates over a block based cdx source.
 * <p>
 * Note: It is mandatory to call {@link #init()} on new instances of this class.
 */
public class BlockCdxSourceIterator implements CdxIterator {

    CdxBuffer cdxBuffer;

    Future<ByteBuffer> nextBuffer;

    final ExecutorService executorService = CdxSourceExecutorService.getInstance();

    final byte[] startFilter;

    final byte[] endFilter;

    final CdxLineFormatMapper lineFormatMapper;

    boolean eof = false;

    final SourceDescriptor sourceDescriptor;

    final Iterator<SourceBlock> blockIterator;

    CdxLine nextLine = null;

    public BlockCdxSourceIterator(final SourceDescriptor sourceDescriptor,
            final Iterator<SourceBlock> blockIterator,
            final String startKey, final String endKey,
            final CdxLineFormatMapper lineFormatMapper) {

        this.sourceDescriptor = sourceDescriptor;

        if (startKey == null || startKey.isEmpty()) {
            this.startFilter = null;
        } else {
            this.startFilter = startKey.getBytes();
        }

        if (endKey == null || endKey.isEmpty()) {
            this.endFilter = null;
        } else {
            this.endFilter = endKey.getBytes();
        }

        this.lineFormatMapper = lineFormatMapper;

        this.blockIterator = blockIterator;

    }

    /**
     * Initialize this iterator.
     * <p>
     * This method must be called before executing any other methods.
     * <p>
     * @return this iterator for easy chaining
     */
    BlockCdxSourceIterator init() {
        cdxBuffer = new CdxBuffer(lineFormatMapper, startFilter, endFilter);
        fillBuffer();
        skipLines();
        return this;
    }

    /**
     * Release resources.
     */
    @Override
    public void close() {
        eof = true;
        cdxBuffer = null;
    }

    final boolean fillBuffer() {
        if (nextBuffer == null && !blockIterator.hasNext()) {
            return false;
        }

        ByteBuffer recycleableBuffer = null;

        try {
            if (cdxBuffer.getByteBuf() == null) {
                cdxBuffer.setByteBuf(fillBuffer(blockIterator.next(), cdxBuffer.getByteBuf()).get());
            } else if (nextBuffer != null) {
                ByteBuffer next = nextBuffer.get();
                nextBuffer = null;

                recycleableBuffer = cdxBuffer.getByteBuf();
                cdxBuffer.setByteBuf(next);
            }
        } catch (InterruptedException ex) {
            return false;
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        if (blockIterator.hasNext()) {
            nextBuffer = fillBuffer(blockIterator.next(), recycleableBuffer);
        }

        return cdxBuffer != null;
    }

    Future<ByteBuffer> fillBuffer(final SourceBlock currentBlock, final ByteBuffer recycledBuffer) {
        return executorService.submit(new Callable<ByteBuffer>() {
            @Override
            public ByteBuffer call() throws Exception {
                try {
                    return sourceDescriptor.read(currentBlock, recycledBuffer);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

        });
    }

    /**
     * If a start filter exist, skip lines not matching filter.
     */
    final void skipLines() {
        do {
            if (cdxBuffer.skipLines()) {
                return;
            }

            eof = !fillBuffer();
        } while (!eof);
    }

    @Override
    public boolean hasNext() {
        if (nextLine != null) {
            return true;
        } else if (eof) {
            return false;
        } else {
            if (!blockIterator.hasNext()) {
                nextLine = readLineCheckingFilter();
            } else {
                nextLine = readLine();
            }

            return (nextLine != null);
        }
    }

    @Override
    public CdxLine next() {
        if (nextLine != null || hasNext()) {
            CdxLine line = nextLine;
            nextLine = null;
            return line;
        } else {
            return null;
        }
    }

    @Override
    public CdxLine peek() {
        if (hasNext()) {
            return nextLine;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Iterator is read only.");
    }

    /**
     * Read the next line of cdx data.
     * <p>
     * This method delegates to the buffer to read the next line. This method takes care of loading
     * the next buffer if end of current buffer was reached.
     * <p>
     * @return the next line or null if end of result set
     */
    CdxLine readLine() {
        do {
            CdxLine cdx = cdxBuffer.readLine();
            if (cdx != null) {
                return cdx;
            }

            eof = !fillBuffer();
        } while (!eof);

        return null;
    }

    /**
     * Read the next line of cdx data ensuring the line is within the requested range.
     * <p>
     * This method delegates to the buffer to read the next line. This method takes care of loading
     * the next buffer if end of current buffer was reached.
     * <p>
     * @return the next line or null if end of result set
     */
    CdxLine readLineCheckingFilter() {
        do {
            CdxLine cdx = cdxBuffer.readLineCheckingFilter();
            if (cdx != null) {
                return cdx;
            }

            eof = !fillBuffer();
        } while (!eof);

        return null;
    }

}
