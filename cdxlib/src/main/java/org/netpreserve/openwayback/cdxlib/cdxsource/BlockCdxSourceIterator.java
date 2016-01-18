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
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;

/**
 *
 */
public class BlockCdxSourceIterator implements CdxIterator {

    final byte[] startFilter;

    final byte[] endFilter;

    CdxLine nextLine = null;

    ByteBuffer byteBuf;

    final int delimiter;

    final CdxLineFormatMapper lineFormatMapper;

    boolean eof = false;

    final SourceDescriptor sourceDescriptor;

    final List<SourceBlock> blocks;

    public BlockCdxSourceIterator(final SourceDescriptor sourceDescriptor, final String startUrl,
            final String endUrl, final CdxLineFormatMapper lineFormatMapper) {

        this.sourceDescriptor = sourceDescriptor;

        if (startUrl == null || startUrl.isEmpty()) {
            this.startFilter = null;
        } else {
            this.startFilter = startUrl.getBytes();
        }

        if (endUrl == null || endUrl.isEmpty()) {
            this.endFilter = null;
        } else {
            this.endFilter = endUrl.getBytes();
        }

        this.lineFormatMapper = lineFormatMapper;

        blocks = sourceDescriptor.calculateBlocks(startUrl, endUrl);

        fillBuffer();

        delimiter = lineFormatMapper.getInputFormat().getDelimiter();

        skipLines();
    }

    @Override
    public boolean hasNext() {
        if (nextLine != null) {
            return true;
        } else if (eof) {
            return false;
        } else {
            if (endFilter != null && blocks.isEmpty()) {
                nextLine = readLine(endFilter);
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
    public void close() {
        eof = true;
        byteBuf = null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Iterator is read only.");
    }

    boolean fillBuffer() {
        try {
            if (blocks.isEmpty()) {
                return false;
            }

            SourceBlock block;
            block = blocks.remove(0);

            byteBuf = sourceDescriptor.read(block, byteBuf);
            return byteBuf != null;

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    CdxLine readLine() {
        do {
            byteBuf.mark();

            if (byteBuf.hasRemaining()) {
                skipToEndOfLine();
                int endOfLine = byteBuf.position();
                byteBuf.reset();
                CdxLine cdxLine = createCdxLine(byteBuf, endOfLine, lineFormatMapper);
                skipLF();
                return cdxLine;
            }

            eof = !fillBuffer();
        } while (!eof);

        return null;
    }

    CdxLine readLine(final byte[] endFilter) {
        do {
            byteBuf.mark();
            if (byteBuf.hasRemaining()) {
                if (compareToFilter(endFilter) < 0) {
                    skipToEndOfLine();
                    int endOfLine = byteBuf.position();
                    byteBuf.reset();
                    CdxLine cdxLine = createCdxLine(byteBuf, endOfLine, lineFormatMapper);
                    skipLF();
                    return cdxLine;
                }
            }

            eof = !fillBuffer();
        } while (!eof);

        return null;
    }

    /**
     * Check for newline chracters.
     *
     * @param c the character to check
     * @return true if LF or CR
     */
    boolean isLf(int c) {
        return c == '\n' || c == '\r';
    }

    int compareToFilter(final byte[] filter) {
        int filterLength = filter.length;

        int k = 0;

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int offset = byteBuf.position();
            int limit = byteBuf.limit();

            for (int i = offset; i < limit && k < filterLength; i++) {
                byte c = buf[i];
                byte cf = filter[k];

                if (c == delimiter) {
                    byteBuf.position(i + 1);
                    break;
                }
                if (c != cf) {
                    byteBuf.position(i + 1);
                    return c - cf;
                }
                k++;
            }
        } else {
            while (k < filterLength && byteBuf.hasRemaining()) {
                byte c = byteBuf.get();
                byte cf = filter[k];

                if (c == delimiter) {
                    break;
                }
                if (c != cf) {
                    return c - cf;
                }
                k++;
            }
        }

        return k - filterLength;
    }

    void skipLines() {
        if (startFilter == null) {
            return;
        }

        do {
            while (byteBuf.hasRemaining()) {
                byteBuf.mark();
                if (compareToFilter(startFilter) < 0) {
                    skipToEndOfLine();
                    skipLF();
                } else {
                    byteBuf.reset();
                    return;
                }
            }

            eof = !fillBuffer();
        } while (!eof);
    }

    void skipToEndOfLine() {
        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int offset = byteBuf.position();
            int limit = byteBuf.limit();

            for (int i = offset; i < limit; i++) {
                if (isLf(buf[i])) {
                    byteBuf.position(i);
                    return;
                }
            }
        } else {
            while (byteBuf.hasRemaining()) {
                if (isLf(byteBuf.get())) {
                    byteBuf.position(byteBuf.position() - 1);
                    return;
                }
            }
        }
    }

    void skipLF() {
        if (isLf(byteBuf.get())) {
            // Check for extra line feed
            if (byteBuf.hasRemaining() && byteBuf.get(byteBuf.position()) == '\n') {
                byteBuf.get();
            }
            return;
        }
    }

    static CdxLine createCdxLine(final ByteBuffer byteBuf, final int startOfNextLine,
            final CdxLineFormatMapper outputFormat) {

        try {
            return new CdxLine(convertToCharBuffer(byteBuf, startOfNextLine), outputFormat);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    static CharBuffer convertToCharBuffer(final ByteBuffer byteBuf,
            final int startOfNextLine) {

        int lineLength = startOfNextLine - byteBuf.position();
        char[] out = new char[lineLength];

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int offset = byteBuf.position() + byteBuf.arrayOffset();

            for (int i = 0; i < lineLength; i++) {
                byte c = buf[i + offset];
                if (c < 0) {
                    // Line contains non ascii character, must decode line.
                    return convertToCharBufferNonAscii(byteBuf, lineLength);
                }
                out[i] = (char) c;
            }
            byteBuf.position(startOfNextLine);
        } else {
            for (int i = 0; i < lineLength; i++) {
                byte c = byteBuf.get();
                if (c < 0) {
                    // Line contains non ascii character, must decode line.
                    byteBuf.reset();
                    return convertToCharBufferNonAscii(byteBuf, lineLength);
                }
                out[i] = (char) c;
            }
        }
        return CharBuffer.wrap(out);
    }

    static CharBuffer convertToCharBufferNonAscii(final ByteBuffer byteBuf,
            final int lineLength) {

        ByteBuffer line = byteBuf.slice();
        line.limit(lineLength);
        return StandardCharsets.UTF_8.decode(line);
    }

}
