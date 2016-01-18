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

import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;

import static org.netpreserve.openwayback.cdxlib.cdxsource.BlockCdxSourceIterator.createCdxLine;

/**
 *
 */
public class BlockCdxSourceReverseIterator extends BlockCdxSourceIterator {

    /**
     * End of block. Actually reached the beginning of block since we are going backwards.
     */
    private boolean eob = false;

    private int endOfLine;

    public BlockCdxSourceReverseIterator(final SourceDescriptor sourceDescriptor,
            final String startUrl, final String endUrl,
            final CdxLineFormatMapper lineFormatMapper) {

        super(sourceDescriptor, startUrl, endUrl, lineFormatMapper);
    }

    @Override
    public boolean hasNext() {
        if (nextLine != null) {
            return true;
        } else if (eof) {
            return false;
        } else {
            if (startFilter != null && blocks.isEmpty()) {
                nextLine = readLine(startFilter);
            } else {
                nextLine = readLine();
            }

            return (nextLine != null);
        }
    }

    @Override
    void skipLines() {
        if (endFilter == null) {
            return;
        }

        byteBuf.mark();
        while (compareToFilter(endFilter) >= 0) {
            byteBuf.reset();
            skipLF();
            endOfLine = byteBuf.position();
            skipToPreviousLine();
            byteBuf.mark();
        }

        byteBuf.reset();
    }

    @Override
    void skipLF() {
        int position = byteBuf.position() - 1;
        while (position >= 0 && isLf(byteBuf.get(position))) {
            position--;
        }
        byteBuf.position(position + 1);
    }

    @Override
    boolean fillBuffer() {
        try {
            if (blocks.isEmpty()) {
                return false;
            }

            SourceBlock block;
            block = blocks.remove(blocks.size() - 1);

            byteBuf = sourceDescriptor.read(block, byteBuf);

            if (byteBuf == null) {
                return false;
            } else {
                eob = false;
                byteBuf.position(byteBuf.limit());
                skipLF();
                endOfLine = byteBuf.position();
                skipToPreviousLine();
                return true;
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    CdxLine readLine() {
        do {
            byteBuf.mark();

            if (!eob) {
                CdxLine cdxLine = createCdxLine(byteBuf, endOfLine, lineFormatMapper);
                byteBuf.reset();
                if (byteBuf.position() == 0) {
                    eob = true;
                }
                if (!eob) {
                    skipLF();
                    endOfLine = byteBuf.position();
                    skipToPreviousLine();
                }
                return cdxLine;
            }

            eof = !fillBuffer();
        } while (!eof);

        return null;
    }

    @Override
    CdxLine readLine(final byte[] startFilter) {
        do {
            byteBuf.mark();
            if (!eob) {
                if (compareToFilter(startFilter) >= 0) {
                    byteBuf.reset();
                    CdxLine cdxLine = createCdxLine(byteBuf, endOfLine, lineFormatMapper);
                    byteBuf.reset();
                    if (byteBuf.position() == 0) {
                        eob = true;
                    }
                    if (!eob) {
                        skipLF();
                        endOfLine = byteBuf.position();
                        skipToPreviousLine();
                    }
                    return cdxLine;
                }
            }

            eof = !fillBuffer();
        } while (!eof);

        return null;
    }

    void skipToPreviousLine() {
        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int position = byteBuf.position() + byteBuf.arrayOffset() - 1;
            int arrayOffset = byteBuf.arrayOffset();

//            while (isLf(buf[position])) {
//                position--;
//            }

            while (position >= arrayOffset) {
                if (isLf(buf[position])) {
                    byteBuf.position(position + 1);
                    return;
                }
                position--;
            }
            byteBuf.position(arrayOffset);

        } else {
            int position = byteBuf.position() - 1;
//            while (isLf(byteBuf.get(position))) {
//                position--;
//            }

            while (position >= 0) {
                if (isLf(byteBuf.get(position))) {
                    byteBuf.position(position + 1);
                    return;
                }
                position--;
            }
            byteBuf.position(0);
        }
    }

}
