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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.netpreserve.openwayback.cdxlib.CdxFormat;

/**
 * Interaction with a {@link CdxSource}'s underlying data.
 */
public interface SourceDescriptor extends Closeable {

    /**
     * Get a list of blocks containing all lines between fromKey (inclusive) and toKey (exclusive).
     * <p>
     * @param fromKey the key giving a starting point or null for starting at the beginning.
     * @param toKey the key giving a ending point or null for unlimited.
     * @return the list of blocks.
     */
    List<SourceBlock> calculateBlocks(String fromKey, String toKey);

    /**
     * Get the input format for this source.
     * <p>
     * For line oriented CDX files this is the result of parsing the first line in the file.
     * <p>
     * @return the input format.
     */
    CdxFormat getInputFormat();

    /**
     * Read data from a block.
     * <p>
     * @param block the block containing information for the data to read. A null value will simply
     * return null.
     * @param byteBuf a buffer which can be reused for reading. If null, this method should allocate
     * a new buffer.
     * @return the buffer containing the requested data or null if there is no more data to read.
     * Even if a buffer is supplied, there is no guarantee that this will be the returned buffer.
     * @throws IOException is thrown if something went wrong while reading data.
     */
    ByteBuffer read(final SourceBlock block, final ByteBuffer byteBuf) throws IOException;

}
