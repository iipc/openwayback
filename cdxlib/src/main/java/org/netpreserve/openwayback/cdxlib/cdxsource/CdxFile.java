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
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import org.netpreserve.openwayback.cdxlib.SearchResult;
import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineFormatMapper;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.processor.Processor;
import org.netpreserve.openwayback.cdxlib.CdxSource;

/**
 *
 */
public class CdxFile implements CdxSource {

    final SourceDescriptor sourceDescriptor;

    public CdxFile(SourceDescriptor sourceDescriptor) throws IOException {
        this.sourceDescriptor = sourceDescriptor;
    }

    @Override
    public SearchResult search(final String startUrl, final String endUrl,
            final CdxLineSchema outputFormat, final List<Processor> processors) {

        final CdxLineFormatMapper lineFormatMapper = new CdxLineFormatMapper(
                sourceDescriptor.getInputFormat(), outputFormat);

        return new SearchResult() {
            @Override
            public CdxIterator iterator() {
                CdxIterator iterator = new InternalIterator(sourceDescriptor, startUrl,
                        endUrl, lineFormatMapper);

                if (processors != null) {
                    for (Processor processorProvider : processors) {
                        iterator = processorProvider.processorIterator(iterator);
                    }
                }

                return iterator;
            }

        };
    }

    @Override
    public void close() {
        try {
            sourceDescriptor.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final class InternalIterator implements CdxIterator {

        private final byte[] startFilter;

        private final byte[] endFilter;

        private CdxLine nextLine = null;

        private ByteBuffer byteBuf;

        private final int delimiter;

        private final CdxLineFormatMapper lineFormatMapper;

        private boolean skip;

        private boolean eof = false;

        private byte[] inBuf;

        private int inPos;

        private int inLength;

        final SourceDescriptor sourceDescriptor;

        private final List<SourceBlock> blocks;

        public InternalIterator(final SourceDescriptor sourceDescriptor, final String startUrl,
                final String endUrl, final CdxLineFormatMapper lineFormatMapper) {

            this.sourceDescriptor = sourceDescriptor;

            if (startUrl == null || startUrl.isEmpty()) {
                this.startFilter = null;
                skip = false;
            } else {
                this.startFilter = startUrl.getBytes();
                skip = true;
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

            skipLines(startFilter);
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
                throw new NoSuchElementException();
            }
        }

        @Override
        public CdxLine peek() {
            if (nextLine != null || hasNext()) {
                return nextLine;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Iterator is read only.");
        }

        private boolean fillBuffer() {
            try {
                if (blocks.isEmpty()) {
                    return false;
                }

                SourceBlock block = blocks.remove(0);

                if ((byteBuf = sourceDescriptor.read(block, byteBuf)) != null) {
                    inBuf = byteBuf.array();
                    inPos = byteBuf.arrayOffset();
                    inLength = byteBuf.arrayOffset() + byteBuf.limit();

                    return true;
                } else {
                    return false;
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        private CdxLine readLine() {
            do {
                int lineStart = inPos;
                final byte[] inBuf = this.inBuf;

                if (inPos < inLength) {
                    int len = lengthToEndOfLine(inBuf, lineStart);
                    CdxLine cdxLine = createCdxLine(inBuf, lineStart, len, lineFormatMapper);
//                    System.out.println(len + " LL >>'" + new String(inBuf, lineStart, len) + "'<<");
//                    inPos = lineStart + len + getLineEndingSize(inBuf, lineStart + len);
                    inPos += len;
                    inPos += getLineEndingSize(inBuf, inPos);
                    return cdxLine;
                }

                eof = !fillBuffer();
            } while (!eof);

            return null;
        }

        private CdxLine readLine(final byte[] endFilter) {
            final byte[] inBuf = this.inBuf;

            do {
                if (inPos < inLength) {
                    return checkEndFilter(inBuf, inPos, endFilter);
                }

                eof = !fillBuffer();
            } while (!eof);

            return null;
        }

        private int getLineEndingSize(final byte[] inBuf, final int offset) {
            if (offset == inBuf.length - 1 || inBuf[offset + 1] != '\n') {
                return 1;
            } else {
                return 2;
            }
        }

        private boolean isLf(int c) {
            return c == '\n' || c == '\r';
        }

        private boolean checkStartFilter(byte toTest, byte[] filter, int pos) {
            if (pos >= filter.length) {
                skip = false;
                return false;
            }

            if (Integer.compare(toTest, filter[pos]) < 0) {
                return true;
            }

            if (Integer.compare(toTest, filter[pos]) > 0) {
                skip = false;
                return false;
            }

            return false;
        }

        private void skipLines(byte[] filter) {
            final byte[] inBuf = this.inBuf;
            int pos = 0;

            do {
                while (skip && inPos < inLength) {
                    byte c = inBuf[inPos++];

                    if (checkStartFilter(c, startFilter, pos++)) {
                        skipToNextLine();
                        pos = 0;
                    }
                }

                if (!skip) {
                    inPos = inPos - pos;
                    return;
                }

                eof = !fillBuffer();
            } while (!eof);
        }

        private void skipToNextLine() {
            inPos += lengthToEndOfLine(inBuf, inPos);
            inPos += getLineEndingSize(inBuf, inPos);
        }

        private int lengthToEndOfLine(final byte[] inBuf, final int offset) {
            for (int i = offset; i < inLength; i++) {
                if (isLf(inBuf[i])) {
                    return i - offset;
                }
            }
            System.out.println("EOFXX");
            return inLength - offset;
        }

        private int findFieldLength(final byte[] inBuf, final int offset) {
            for (int i = offset; i < inLength; i++) {
                if (inBuf[i] == delimiter) {
//                    System.out.println("I " + (i - offset));
                    return i - offset;
                }
                if (isLf(inBuf[i])) {
                    System.out.println("**********************************");
                }
            }
            System.out.println("XX");
            return inLength - offset;
        }

        private int compareByteArray(final byte[] first, final int offset, final int length, final byte[] second) {
            int len1 = length;
            int len2 = second.length;
            int lim = Math.min(len1, len2);

            int k = 0;
            while (k < lim) {
                byte c1 = first[k + offset];
                byte c2 = second[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }

        private CdxLine checkEndFilter(final byte[] inBuf, final int offset, final byte[] filter) {
            int len = findFieldLength(inBuf, offset);
            int comp = compareByteArray(inBuf, offset, len, filter);
            if (comp < 0) {
                len += lengthToEndOfLine(inBuf, offset + len);
                CdxLine cdxLine = createCdxLine(inBuf, offset, len, lineFormatMapper);
//                System.out.println(len + " EF >>'" + new String(inBuf, lineStart, len) + "'<<");
                inPos += len + getLineEndingSize(inBuf, offset + len);
                return cdxLine;
            }

            eof = true;
            return null;
        }

        private static CdxLine createCdxLine(final byte[] inBuf, final int offset,
                final int lineLength, final CdxLineFormatMapper lineFormatMapper) {

            return new CdxLine(convertToCharBuffer(inBuf, offset, lineLength), lineFormatMapper);
        }

        private static CharBuffer convertToCharBuffer(final byte[] inBuf, final int lineOffset,
                final int lineLength) {

            char[] out = new char[lineLength];
            for (int i = 0; i < lineLength; i++) {
                if (inBuf[i] < 0) {
                    // Line contains non ascii character, must decode line.
                    return convertToCharBufferNonAscii(inBuf, lineOffset, lineLength);
                }
                out[i] = (char) inBuf[i + lineOffset];
            }
            return CharBuffer.wrap(out);
        }

        private static CharBuffer convertToCharBufferNonAscii(final byte[] inBuf,
                final int lineOffset, final int lineLength) {

            ByteBuffer line = ByteBuffer.wrap(inBuf, lineOffset, lineLength);
            return StandardCharsets.UTF_8.decode(line);
        }

    }
}
