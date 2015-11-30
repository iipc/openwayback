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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.netpreserve.openwayback.cdxlib.CdxLineSchema;

/**
 * Metadata for a local CDX file.
 */
public class CdxFileDescriptor implements SourceDescriptor {

    private static final int BUFFER_SIZE = 1024 * 1024 * 16;

    private static final int BLOCK_SIZE = 1024 * 128;

    final FileChannel channel;

    CdxLineSchema inputFormat;

    final long channelSize;

    final ArrayList<SourceBlock> blocks;

    /**
     * Constructor for creating metadata for å file.
     * <p>
     * @param path the {@link Path} to the CDX file.
     * @throws IOException if failing reading file.
     */
    public CdxFileDescriptor(Path path) throws IOException {
        this.channel = FileChannel.open(path, StandardOpenOption.READ);
        this.channelSize = channel.size();
        int blockNum = (int) (this.channelSize / BLOCK_SIZE);
        this.blocks = new ArrayList<>(blockNum);

        ByteBuffer inBuf = ByteBuffer.allocate(1024 * 8);
        SourceBlock prevBlock = null;

        for (int i = 0; i < blockNum; i++) {
            long bufferOffset = i * BLOCK_SIZE;
            inBuf.rewind();
            channel.read(inBuf, bufferOffset);
            inBuf.flip();

            // Find start of line
            skipToNextLine(inBuf);

            if (i == 0) {
                // First line contains CDX format string
                String formatLine = new String(inBuf.array(),
                        inBuf.arrayOffset(),
                        inBuf.arrayOffset() + inBuf.position());

                inputFormat = new CdxLineSchema(formatLine);
            }

            int lineOffset = inBuf.position();
            Long fileOffset = bufferOffset + lineOffset;
            int fieldLength = getLengthOfField(inBuf, ' ');

            SourceBlock block = new SourceBlock(new String(inBuf.array(), lineOffset, fieldLength),
                    fileOffset, 0);

            blocks.add(block);
            if (prevBlock != null) {
                prevBlock.length = (int) (block.offset - prevBlock.offset);
            }
            prevBlock = block;
        }

        prevBlock.length = (int) (channelSize - prevBlock.offset);
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
            if (block.length + nextBlock.length < BUFFER_SIZE) {
                block.length += nextBlock.length;
            } else {
                if (toKey != null && toKey.compareTo(nextBlock.key) < 0) {
                    break;
                } else {
                    block = nextBlock.clone();
                    result.add(block);
                }
            }
        }

        return result;
    }

    @Override
    public CdxLineSchema getInputFormat() {
        return inputFormat;
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

    /**
     * Move buffer position to beginning of next line.
     * <p>
     * @param inBuf the buffer to process.
     */
    private void skipToNextLine(final ByteBuffer inBuf) {
        while (inBuf.hasRemaining()) {
            byte c = inBuf.get();

            if (c == '\n' || c == '\r') {
                if (inBuf.get(inBuf.position()) == '\n') {
                    inBuf.get();
                }
                return;
            }
        }
    }

    /**
     * Move buffer position to next field delimiter and return the number of bytes skipped.
     * <p>
     * @param inBuf the buffer to process.
     * @param delimiter the field delimiter to search for.
     * @return the number of bytes skipped.
     */
    private int getLengthOfField(ByteBuffer inBuf, char delimiter) {
        int len = 0;
        while (inBuf.hasRemaining() && inBuf.get() != delimiter) {
            len++;
        }
        return len;
    }

    @Override
    public ByteBuffer read(SourceBlock block, ByteBuffer byteBuf) throws IOException {
        if (block == null) {
            return null;
        }

        if (byteBuf == null || byteBuf.capacity() < (BLOCK_SIZE * 2)) {
            byteBuf = ByteBuffer.allocate(BUFFER_SIZE);
        }

        byteBuf.clear();
        byteBuf.limit(block.length);

        if (channel.read(byteBuf, block.offset) < 0) {
            // This should not happen since we know the block size.
            return null;
        }

        byteBuf.flip();

        return byteBuf;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

}
