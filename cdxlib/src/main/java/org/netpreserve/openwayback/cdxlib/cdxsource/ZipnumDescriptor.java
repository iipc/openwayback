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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.netpreserve.openwayback.cdxlib.CdxFormat;

/**
 *
 */
public class ZipnumDescriptor implements SourceDescriptor {

    private static final int BUFFER_SIZE = 1024 * 1024;

    private final CdxFormat inputFormat;

    private Path baseDirectory;

    private Path summaryFile;

    private Path locationFile;

    final ArrayList<SourceBlock> blocks;

    private static final int MINIMUM_BUFFER_SIZE = 3000 * 256;

    public ZipnumDescriptor(Path baseDirectory, CdxFormat inputFormat) {
        this.inputFormat = inputFormat;
        this.blocks = new ArrayList<>(1024);
        this.baseDirectory = baseDirectory;
        this.summaryFile = baseDirectory.resolve("cluster.summary");
        this.locationFile = baseDirectory.resolve("cluster.loc");

        try (InputStream in = Files.newInputStream(summaryFile);) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            while (line != null) {
                String[] fields = line.split("\t");

                SourceBlock block = new SourceBlock(fields[0], Long.parseLong(fields[2]), Integer
                        .parseInt(fields[3]), fields[1]);
                blocks.add(block);

                line = reader.readLine();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<SourceBlock> calculateBlocks(String fromKey, String toKey) {
        ArrayList<SourceBlock> result = new ArrayList<>();

        int firstIdx = findFirstBlockIndex(fromKey);

        // make sure that empty string is handled as null
        toKey = toKey == null || toKey.isEmpty() ? null : toKey;

        SourceBlock block = blocks.get(firstIdx).clone();
        result.add(block);

        for (int i = firstIdx + 1; i < blocks.size(); i++) {
            SourceBlock nextBlock = blocks.get(i);
            if (toKey != null && toKey.compareTo(nextBlock.key) < 0) {
                break;
            } else {
                block = nextBlock.clone();
                result.add(block);
            }
        }

        return result;
    }

    /**
     * Find the index of the first block containing the fromKey. If fromKey is null, index of the
     * first block is returned.
     * <p>
     * @param fromKey the starting point.
     * @return index of first block matching fromKey.
     */
    private int findFirstBlockIndex(String fromKey) {
        if (fromKey == null || fromKey.isEmpty()) {
            return 0;
        }

        for (int i = 1; i < blocks.size(); i++) {
            if (fromKey.compareTo(blocks.get(i).key) <= 0) {
                return i - 1;
            }
        }
        return blocks.size() - 1;
    }

    @Override
    public CdxFormat getInputFormat() {
        return inputFormat;
    }

    long time;

    @Override
    public ByteBuffer read(SourceBlock block, ByteBuffer byteBuf) throws IOException {
        if (block == null) {
            return null;
        }

        if (byteBuf == null || byteBuf.capacity() < (BUFFER_SIZE)) {
            byteBuf = ByteBuffer.allocate(BUFFER_SIZE);
        }

        byteBuf.clear();

        Path path = baseDirectory.resolve(block.location);

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);) {
            ByteBuffer inBuf = ByteBuffer.allocate(block.getLength());
            channel.read(inBuf, block.getOffset());

            try (GZIPInputStream gzip = new GZIPInputStream(
                    new ByteArrayInputStream(inBuf.array()), block.getLength());) {

                int offset = byteBuf.arrayOffset();
                int remainingSpace = byteBuf.limit();

                int len = gzip.read(byteBuf.array(), offset, remainingSpace);
                while (len >= 0) {
                    offset += len;
                    remainingSpace -= len;

                    if (remainingSpace <= 0) {
                        byteBuf = ByteBuffer.allocate(byteBuf.capacity() * 2);
                        return read(block, byteBuf);
                    }
                    len = gzip.read(byteBuf.array(), offset, remainingSpace);
                }

                byteBuf.limit(offset - byteBuf.arrayOffset());
            }
        }
        return byteBuf;
    }

    @Override
    public void close() throws IOException {
    }

    private static class SubInputStream extends FilterInputStream {

        private final long length;

        private long position = 0;

        public SubInputStream(InputStream in, long offset, long length) throws IOException {
            super(fastForward(in, offset));
            this.length = length;
        }

        private static InputStream fastForward(InputStream in, long offset) throws IOException {
            in.skip(offset);
            return in;
        }

        @Override
        public int available() throws IOException {
            return Math.min((int) (length - position), super.available());
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = super.skip(n);
            position += skipped;
            return skipped;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (position >= length) {
                return -1;
            }
            int l = super.read(b, off, len);
            int result = Math.min((int) (length - position), l);
            position += l;
            return result;
        }

        @Override
        public int read() throws IOException {
            if (position >= length) {
                return -1;
            }
            position++;
            return super.read();
        }

    }
}
